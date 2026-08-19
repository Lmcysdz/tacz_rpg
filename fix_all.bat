@echo off
chcp 65001 >nul
cd /d "E:\ideaCode\TaczRPG1.20.1"

echo [1/3] Removing BOM and fixing literal  in Java files...
powershell -NoProfile -Command "Get-ChildItem src -Recurse -Filter *.java | ForEach-Object { $t = [System.IO.File]::ReadAllText($_.FullName); $c = $t; if ($c.Contains([char]92 + 'n')) { $c = $c.Replace([char]92 + 'n', [char]10) }; $b = [System.IO.File]::ReadAllBytes($_.FullName); if ($b[0] -eq 0xEF -and $b[1] -eq 0xBB -and $b[2] -eq 0xBF) { $c = [System.Text.Encoding]::UTF8.GetString($b[3..($b.Length-1)]) }; if ($c -ne $t) { [System.IO.File]::WriteAllText($_.FullName, $c, [System.Text.Encoding]::UTF8); Write-Host ('Fixed: ' + $_.Name) } }; Write-Host 'Done'"

echo [2/3] Compiling...
call gradlew.bat compileJava

if %ERRORLEVEL% EQU 0 (
    echo ======================
    echo BUILD SUCCESSFUL
    echo ======================
) else (
    echo ======================
    echo BUILD FAILED - see errors above
    echo ======================
    pause
)