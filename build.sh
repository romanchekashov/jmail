RESOURCES=src/main/resources/images
TEMP=target

if [[ "$OSTYPE" == "linux-gnu"* ]]; then
  echo "linux-gnu"
elif [[ "$OSTYPE" == "darwin"* ]]; then
  mkdir -p $TEMP/jmail.iconset
  cp $RESOURCES/mail.128x128.png $TEMP/jmail.iconset/icon_128x128.png
  cp $RESOURCES/mail.256x256.png $TEMP/jmail.iconset/icon_256x256.png
  cp $RESOURCES/mail.512x512.png $TEMP/jmail.iconset/icon_512x512.png
  iconutil -c icns $TEMP/jmail.iconset -o $TEMP/jmail.iconset/jmail.icns
  echo "Mac OSX: darwin"
elif [[ "$OSTYPE" == "cygwin" ]]; then
  # POSIX compatibility layer and Linux environment emulation for Windows
  echo "cygwin"
elif [[ "$OSTYPE" == "msys" ]]; then
  # Lightweight shell and GNU utilities compiled for Windows (part of MinGW)
  echo "msys"
elif [[ "$OSTYPE" == "win32" ]]; then
  echo "win32"
elif [[ "$OSTYPE" == "freebsd"* ]]; then
  echo "freebsd"
else
  echo "Unknown"
fi
