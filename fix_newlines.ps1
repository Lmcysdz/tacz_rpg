$root = "E:\ideaCode\TaczRPG1.20.1\src\main\java\com\lmcysdz\taczrpg"
Get-ChildItem -Path $root -Recurse -Filter *.java | ForEach-Object {
    $c = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)
    if ($c.Contains("")) {
        $c = $c.Replace("", "`n")
        [System.IO.File]::WriteAllText($_.FullName, $c, [System.Text.Encoding]::UTF8)
        Write-Host "Fixed: $($_.Name)"
    }
}
Write-Host "Done"