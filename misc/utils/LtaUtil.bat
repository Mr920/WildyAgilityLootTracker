@rem I don't know if this will become programmatic notes or an actual script utility
@rem but I will try to make it fit whatever purpose it ends up taking on
@rem Until I'm certain of this thing, let's make it do (almost) nothing
goto END_OF_FILE

:setUserPathAssumptions
set "P_U_INTELLIJ_BIN=%ProgramFiles%\JetBrains\IntelliJ IDEA 2026.1.3\bin"
set "P_U_WINAPPS=%LOCALAPPDATA%\Microsoft\WindowsApps"
set "P_U_NPM=%APPDATA%\npm"
set "P_U_INTELLIJ=%IntelliJ IDEA%"
set "P_U_ALL=%P_U_WINAPPS%;%P_U_NPM%;%P_U_INTELLIJ%"

:setSystemPathAssumptions
set "P_S_JAVABASE=%HOMEDRIVE%\JAVA"
set "P_S_JDK26_BASE=%P_S_JAVABASE%\openjdk-26.0.1_windows-x64_bin\jdk-26.0.1"
set "P_S_JDK26_BIN=%P_S_JDK26_BASE%\bin"
set "P_S_JAVAFX_JDK26_BASE=%P_S_JAVABASE%\openjfx-26.0.1_windows-x64_bin-sdk\javafx-sdk-26.0.1"
set "P_S_ECLIPSE_JDK11_BASE=%ProgramFiles%\Eclipse Adoptium\jdk-11.0.31.11-hotspot"
set "P_S_ECLIPSE_JRE11_BASE=%ProgramFiles%\Eclipse Adoptium\jre-11.0.31.11-hotspot"
set "P_S_ECLIPSE_JDK11_BIN=%P_S_ECLIPSE_JDK11_BASE%\bin"
set "P_S_ECLIPSE_JRE11_BIN=%P_S_ECLIPSE_JRE11_BASE%\bin"
set "P_S_CHOCO_BIN=%ProgramData%\chocolatey\bin"
set "P_S_NODE=%ProgramFiles%\nodejs\"
set "P_S_GITCMD=%ProgramFiles%\Git\cmd"
set "P_S_GRADLEBASE=%HOMEDRIVE%\Gradle"
set "P_S_GRADLE9_BIN=%P_S_GRADLEBASE%\gradle-9.5.1\bin"
set "P_S_COMMONJAVABASE=%CommonProgramFiles(x86)%\Oracle\Java"
set "P_S_COMMONJAVAPATH=%P_S_COMMONJAVABASE%\javapath"
set "P_S_COMMONJAVA8PATH=%P_S_COMMONJAVABASE%\java8path"
set "P_S_SYSROOT=%SystemRoot%"
set "P_S_SYS32=%SystemRoot%\system32"
set "P_S_WBEM=%P_S_SYS32%\Wbem"
set "P_S_PS=%P_S_SYS32%\WindowsPowerShell\v1.0\"
set "P_S_OPENSSH=%P_S_SYS32%\OpenSSH\"
set "P_S_ALL=%P_S_JDK26_BIN%;%P_S_JDK26_BASE%;%P_S_COMMONJAVA8PATH%;%P_S_COMMONJAVAPATH%;%P_S_ECLIPSE_JRE11_BIN%;%P_S_ECLIPSE_JDK11_BIN%;%P_S_SYS32%;%P_S_SYSROOT%;%P_S_WBEM%;%P_S_PS%;%P_S_OPENSSH%;%P_S_CHOCO_BIN%;%P_S_NODE%;%P_S_GRADLE9_BIN%;%P_S_GITCMD%"

:echoUserVarAssumptions
@echo "===== User Variable Assumptions ====="
@echo "IntelliJ IDEA=%P_U_INTELLIJ_BIN%"
@echo "TEMP=%LOCALAPPDATA%\Temp"
@echo "TMP=%TEMP%"
@echo "Path=%P_U_ALL%"

:echoSysVarAssumptions
@echo "===== System Variable Assumptions ====="
@echo "Path=%P_S_ALL%"
@echo "JAVA_HOME=%P_S_ECLIPSE_JDK11_BASE%"

:setMiscUserNetwork
set "M_U_NET_Z620=\\DESKTOP-QEP4LUI"
set "M_U_NET_LENOVOYOGA=\\DESKTOP-IV1J0I2"
set "M_U_NET_ZBOOK=\\DESKTOP-KVEC0K0"

:setMiscUserOldWorkstation
set "M_U_Z620_HOMEDRIVE=%M_U_NET_Z620%\c"
set "M_U_Z620_USERPROFILE=%M_U_Z620_HOMEDRIVE%\Users\Charles Lentz"
set "M_U_Z620_JDKS=%M_U_Z620_USERPROFILE%\.jdks"

:setMiscUserVars
set "M_U_JDKS=%USERPROFILE%\.jdks"
set "M_U_OLDJDKS=%M_U_Z620_JDKS%"
set "M_U_JDK17_BASE=%M_U_Z620_HOMEDRIVE%\Program Files\Java\jdk-17"
set "M_U_INTELLIJ_JDK_MS11=%M_U_JDKS%\ms-11.0.31"
set "M_U_INTELLIJ_OLD_JDK_MS11=%M_U_OLDJDKS%\ms-11.0.31"
set "M_U_INTELLIJ_OLD_JDK_26=%M_U_OLDJDKS%\openjdk-26.0.1"



:END_OF_FILE
@echo ""

