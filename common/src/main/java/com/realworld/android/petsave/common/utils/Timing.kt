package com.realworld.android.petsave.common.utils

class Timing {
    companion object {
        fun doRandomWork() {
            val number = (0..100).random()
            factorial(number)
        }

        /**
         * tailrec (안전 수정자): StackOverFlow를 방지할 수 있다.
         *  - 함수의 마지막 작업은 자신을 호출하는 작업만 가능
         *  - 재귀 호출 후 추가 코드를 작성할 수 없다
         *  - try / catch / finally 블록 내에서 사용을 금지
         */
        private tailrec fun factorial(number: Int, accumulator: Int = 1): Int {
            return when (number) {
                1 -> accumulator
                else -> factorial(number - 1, accumulator * number)
            }
        }
    }
}