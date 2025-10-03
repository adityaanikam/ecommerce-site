# PowerShell script to replace all image URLs with working placeholder URLs
$files = Get-ChildItem -Path "src" -Filter "*.java" -Recurse

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    # Replace via.placeholder.com URLs with placehold.co
    $content = $content -replace 'https://via\.placeholder\.com/800x800/6366f1/ffffff', 'https://placehold.co/800x800/6366f1/ffffff'
    $content = $content -replace 'https://via\.placeholder\.com/800x600/6366f1/ffffff', 'https://placehold.co/800x600/6366f1/ffffff'
    # Replace any remaining picsum URLs
    $content = $content -replace 'https://picsum\.photos/seed/([^/]+)/800/800', 'https://placehold.co/800x800/6366f1/ffffff'
    $content = $content -replace 'https://picsum\.photos/800/600', 'https://placehold.co/800x600/6366f1/ffffff'
    Set-Content $file.FullName -Value $content -NoNewline
    Write-Host "Updated: $($file.FullName)"
}

Write-Host "Fixed placeholder URL replacement completed!"
