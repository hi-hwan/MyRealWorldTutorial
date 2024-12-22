/*
 * Copyright (c) 2022 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.realworld.android.petsave.main.presentation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.dynamicfeatures.fragment.DynamicNavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.realworld.android.petsave.R
import com.realworld.android.petsave.common.data.preferences.PetSavePreferences
import com.realworld.android.petsave.common.data.preferences.Preferences
import com.realworld.android.petsave.common.domain.model.user.User
import com.realworld.android.petsave.common.domain.repositories.UserRepository
import com.realworld.android.petsave.common.utils.FileConstants
import com.realworld.android.petsave.common.utils.PreferencesHelper
import com.realworld.android.petsave.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import com.realworld.android.petsave.animalsnearyou.R as animalsNearYouR
import com.realworld.android.petsave.common.R as commonR
import com.realworld.android.petsave.onboarding.R as onboardingR
import com.realworld.android.petsave.report.R as reportR
import com.realworld.android.petsave.search.R as searchR

/**
 * Main Screen
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding get() = _binding!!
    private var _binding: ActivityMainBinding? = null

    private val viewModel by viewModels<MainActivityViewModel>()

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as DynamicNavHostFragment)
            .navController
    }
    private val appBarConfiguration by lazy {
        AppBarConfiguration(
            topLevelDestinationIds = setOf(
                onboardingR.id.onboardingFragment,
                animalsNearYouR.id.animalsNearYouFragment,
                searchR.id.searchFragment,
                reportR.id.reportFragment
            )
        )
    }
    private var isSignedUp = false
    private var workingFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Switch to AppTheme for displaying the activity
        setTheme(commonR.style.AppTheme)

        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupBottomNav()
        setupWorkingFiles()
        updateLoggedInState()

        subscribeToLogin()
        subscribeToViewEffects()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun setupBottomNav() {
        binding.bottomNavigation.visibility = View.GONE
        binding.bottomNavigation.setupWithNavController(navController)
        hideBottomNavWhenNeeded()
    }

    private fun hideBottomNavWhenNeeded() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                onboardingR.id.onboardingFragment -> binding.bottomNavigation.visibility = View.GONE
                else -> binding.bottomNavigation.visibility = View.VISIBLE
            }
        }
    }

    private fun triggerStartDestinationEvent() {
        viewModel.onEvent(MainActivityEvent.DefineStartDestination)
    }

    private fun subscribeToLogin() {
        lifecycleScope.launch {
            viewModel.isLoggedIn.collect { loggedIn ->
                if (loggedIn) {
                    triggerStartDestinationEvent()
                }
            }
        }
    }

    private fun subscribeToViewEffects() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewEffect.collect { reactTo(it) }
            }
        }
    }

    private fun reactTo(effect: MainActivityViewEffect) {
        when (effect) {
            is MainActivityViewEffect.SetStartDestination -> setNavGraphStartDestination(effect.destination)
        }
    }

    private fun setNavGraphStartDestination(startDestination: Int) {
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        navGraph.setStartDestination(startDestination)
        navController.graph = navGraph
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.theme_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val themeMode = when (item.itemId) {
            R.id.light_theme -> {
                AppCompatDelegate.MODE_NIGHT_NO
            }

            R.id.dark_theme -> {
                AppCompatDelegate.MODE_NIGHT_YES
            }

            else -> {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        }
        AppCompatDelegate.setDefaultNightMode(themeMode)
        return true
    }


    private fun setupWorkingFiles() {
        workingFile = File(filesDir.absolutePath + File.separator +
                FileConstants.DATA_SOURCE_FILE_NAME)
    }

    fun loginPressed(view: View) {
        displayLogin(view, false)
    }

    private fun updateLoggedInState() {
        val fileExists = workingFile?.exists() ?: false
        if (fileExists) {
            isSignedUp = true
            binding.loginButton.text = getString(R.string.login)
            binding.loginEmail.visibility = View.INVISIBLE
        } else {
            binding.loginButton.text = getString(R.string.signup)
        }
    }

    private fun displayLogin(view: View, fallback: Boolean) {
        //TODO: Replace below
        performLoginOperation(view)
    }

    private fun performLoginOperation(view: View) {
        var success = false
        val preferences: Preferences = PetSavePreferences(this)

        workingFile?.let {
            //Check if already signed up
            if (isSignedUp) {
                val fileInputStream = FileInputStream(it)
                val objectInputStream = ObjectInputStream(fileInputStream)
                val list = objectInputStream.readObject() as ArrayList<User>
                val firstUser = list.first() as? User
                if (firstUser is User) { //2
                    //TODO: Replace below with implementation that decrypts password
                    success = true
                }

                if (success) {
                    toast("Last login: ${PreferencesHelper.lastLoggedIn(this)}")
                } else {
                    toast("Please check your credentials and try again.")
                }

                objectInputStream.close()
                fileInputStream.close()
            } else {
                //TODO: Replace with encrypted data source below
                UserRepository.createDataSource(applicationContext, it, ByteArray(0))
                success = true
            }
        }

        if (success) {
            PreferencesHelper.saveLastLoggedInTime(this)
            viewModel.setIsLoggedIn(true)

            //Show fragment
            binding.loginEmail.visibility = View.GONE
            binding.loginButton.visibility = View.GONE
            val fragmentManager = supportFragmentManager
            val fragment = fragmentManager.findFragmentById(R.id.nav_host_fragment)
            fragment?.let{
                fragmentManager.beginTransaction()
                    .show(it)
                    .commit()
            }
            fragmentManager.executePendingTransactions()
            binding.bottomNavigation.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
