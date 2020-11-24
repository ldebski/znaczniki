import os
from PIL import Image, ImageDraw
from pathlib import Path

for screenshots_counter in range(1):
    screenshots_dir = "/to_resize"
    directory_files = os.listdir("." + screenshots_dir)
    if "desktop.ini" in directory_files:
        directory_files.remove("desktop.ini")
    for photo in directory_files:
        if os.path.isfile("." + screenshots_dir + "/" + photo):
            im = Image.open("." + screenshots_dir + "/" + photo)
            img = im.resize((200, 300), Image.ANTIALIAS)
            Path("./resized" + screenshots_dir).mkdir(parents=True, exist_ok=True)
            img.save("./resized" + screenshots_dir + "/" + photo)
