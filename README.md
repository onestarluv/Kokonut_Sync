# Kokonut_Sync

에고버스 전용 Paper 26.3 플레이어 데이터 동기화 플러그인.

- Core와 동일한 버전: `cbt-1.0`, 작성자: `pingpong`, 웹사이트: https://wiki.egoverse.kr
- `Kokonut-Core` 필수. SQL은 CoreDatabase 서비스를 사용하며 Redis 접속 설정은 Core의 RedisSettings를 사용합니다.
- 설정은 Settings 클래스 기본값, 메시지는 JAR 내 한국어 리소스를 사용합니다. 로컬 config.yml/messages/server.yml을 읽거나 생성하지 않습니다.
- 서버 ID는 월드 로딩 후 서버의 첫 번째(메인) 월드 이름입니다. 각 서버의 메인 월드 이름은 서로 달라야 합니다.
- Fabric, 다른 Minecraft 버전, 업데이트 확인, bStats, Plan, ProtocolLib, PacketEvents, 외부 데이터 마이그레이터를 제거했습니다.

빌드: `gradlew.bat :common:test :bukkit:26.3:build`

Core JAR 기본 경로는 `../core/Kokonut-Core/build/libs/Kokonut-Core-cbt-1.0.jar`이며 `-PcoreJar=경로`로 지정할 수 있습니다.
산출물: `target/Kokonut_Sync-Bukkit-cbt-1.0+mc.26.3.jar`.

HuskSync 기반 포크이며 원본 저작권 및 Apache-2.0 라이선스는 유지합니다.
