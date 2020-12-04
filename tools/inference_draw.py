import os
import tensorflow as tf
import matplotlib.pyplot

from PIL import Image, ImageDraw


def draw_lines(path, points, dir):
    im = Image.open(path)
    d = ImageDraw.Draw(im)
    for i in range(0, 16, 2):
        d.ellipse((points[i] - 5, points[i+1] - 5, points[i] + 5, points[i+1] + 5), fill='red', outline='red')

    im.save(dir)


for model_filename in os.listdir("./models"):
    model = tf.keras.models.load_model("./models/" + model_filename)
    counter = 0
    directory = "./inference_source"
    directory_files = os.listdir(directory)
    if "desktop.ini" in directory_files:
        directory_files.remove("desktop.ini")
    for file in directory_files:
        img = matplotlib.pyplot.imread(directory + "/" + file)
        img = img[tf.newaxis, :]
        points = model.predict(img)[0] * 224
        draw_lines(directory + "/" + file, points, "./inference_results/" + model_filename + "___" + str(counter)
                   + ".jpg")
        counter += 1
