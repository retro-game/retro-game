# Take the raw unit data and convert to array
my_array="{0,250,250,100,15,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}"
my_array=${my_array//,/ }  # replace commas with spaces
my_array=${my_array/\{/\(}  # replace starting brace with opening parenthesis
my_array=${my_array/\}/\)}  # replace closing brace with closing parenthesis
echo $my_array   # prints "(0 250 250 100 15 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0)"
my_array[1]=500
my_array[4]=50
echo $my_array   # prints "(0 250 250 100 15 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0)"


# convert array back to string
my_array=${my_array/\(/\{}  # replace opening parenthesis with starting brace
my_array=${my_array/\)/\}}  # replace closing parenthesis with closing brace
my_array=${my_array// /,}   # replace spaces with commas
echo $my_array   # prints "{0,250,250,100,15,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}"

