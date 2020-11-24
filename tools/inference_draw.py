import os
import tensorflow as tf
import matplotlib.pyplot

from PIL import Image, ImageDraw


def draw_lines(path, points, dir):
    im = Image.open(path)
    d = ImageDraw.Draw(im)
    line_color = (0, 0, 255)

    one = (points[0][0] * 200, 300 - points[0][1] * 300)
    two = (points[0][2] * 200, 300 - points[0][3] * 300)
    three = (points[0][4] * 200, 300 - points[0][5] * 300)
    fours = (points[0][6] * 200, 300 - points[0][7] * 300)
    d.line([one, two, three, fours, one], fill=line_color, width=2)
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
        points = model.predict(img)
        draw_lines(directory + "/" + file, points, "./inference_results/" + model_filename + "___" + str(counter)
                   + ".jpg")
        counter += 1
