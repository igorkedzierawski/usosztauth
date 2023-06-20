pkgname=usosztauth
pkgver=1.6
pkgrel=1
pkgdesc="Serwis uwierzytelniający użytkowników do sieci ZeroTier przez USOS"
arch=("x86_64")
conflicts=('usosztauth')
provides=("usosztauth=$pkgver")
install=usosztauth.install
source=('git+https://github.com/igorkedzierawski/usosztauth')
sha256sums=('SKIP')


fatal() {
    echo $@ 1>&2; exit 1;
}
get_java17_or_newer_binary() {
    main_java=$(readlink -f $(which java 2>/dev/null || echo "") 2>/dev/null || echo "")
    [ -z "$main_java" ] && return;
    java_location=$(echo "$main_java" | grep -oP '/[a-z]*/[a-z]*/')
    [ -z "$java_location" ] && return;
    find "$java_location" -name "java" |
        grep -P 'java\-(1[7-9]|[2-9][0-9])' |
        tail -1
}
java_bin=$(get_java17_or_newer_binary)
[ -z $java_bin ] && fatal "Nie znaleziono javy 17 lub nowszej. Nie można zbudować serwisu"

build() {
    cd $pkgname
    ./gradlew jar
}
package() {
    cd $srcdir/$pkgname
    install -dm755 $pkgdir/usr/share/java/usosztauth/
    install -dm755 $pkgdir/usr/share/usosztauth/
    install -dm755 $pkgdir/usr/bin/
    install -dm755 $pkgdir/usr/lib/systemd/system/
    install -dm755 $pkgdir/etc/sysusers.d/
    install -Dm755 build/libs/UsosZtAuth-1.6.jar $pkgdir/usr/share/java/usosztauth/UsosZtAuth.jar
    install -Dm744 usosztauth-service-runner $pkgdir/usr/bin/
    install -Dm755 usosztauth.service $pkgdir/usr/lib/systemd/system/
    install -Dm755 usosztauth.conf.userconf $pkgdir/etc/sysusers.d/usosztauth.conf
}
