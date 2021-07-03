dir "./toconvert" -r | %{
    $path = $_.FullName 
    $new = $path.replace("toconvert", "converted")
    $command = "sox '$path' -c 1 '$new'"
    echo "Processing "$command
    iex $command
}
pause