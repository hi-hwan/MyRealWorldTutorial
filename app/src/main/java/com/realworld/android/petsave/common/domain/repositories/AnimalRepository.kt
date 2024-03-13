package com.realworld.android.petsave.common.domain.repositories


// 리포지토리 패턴은 데이터 소스 추상화를 위한 매우 일반적인 패턴이다.
// 리포지토리를 사용해 원하는 모든 데이터 소스를 추상화할 수 있다.
// 리포지토리를 호출하는 모든 것은 해당 소스의 데이터에 액세스할 수 있지만, 어떤 소스가 존재하는지 알 수는 없다.
//
// 도메인 계층에서는 실제로 리포지토리를 구현하지 않는다. 대신, 리포지토리 인터페이스만 갖게 된다.
// 이를 통해 계층 간 종속성을 반전 (Invert the dependency) 하여
// 데이터 계층이 도메인 계층에 종속되는 것이 아니라 그 반대로 만들 수 있다!
interface AnimalRepository