import os
import pandas as pd
from PIL import Image, ImageFilter, ImageOps, ExifTags


dict_keys = ("Marker1", "Marker2", "Marker3", "Marker4", "ColorLeft", "ColorRight",
                         "GrayScaleRight", "GreyScaleLeft", "CircleTop", "CircleRight", "CircleBottom", "CircleLeft")

image_width = 224
image_height = 224

def append_to_dict(im, row):
    im.save(save_dir_prefix + str(counter[counter_index]) + "." + ext.lower())
    dict_for_csv["photoFullPath"].append(str(counter[counter_index]) + "." + ext.lower())
    for key in dict_keys:
        dict_for_csv[key].append(row[key])
    counter[counter_index] += 1


for i in range(len(os.listdir("./dataset"))):
    counter = [0]
    dict_for_csv = {"photoFullPath": [], "Marker1": [], "Marker2": [], "Marker3": [], "Marker4": [],
                    "ColorLeft": [], "ColorRight": [], "GrayScaleRight": [], "GreyScaleLeft": [],
                    "CircleTop": [], "CircleRight": [], "CircleBottom": [], "CircleLeft": []}
    save_dir_prefix = "./dataset_augmented/data{}/source_to_label/".format(i)
    if not os.path.exists(save_dir_prefix):
        os.makedirs(save_dir_prefix)
    data = pd.read_csv("./dataset/data{}/labeled_data.csv".format(i), sep="|")
    for index, row in data.iterrows():
        file = row["photoFullPath"]
        for degree_mul in range(1):
            counter_index = 0
            ext = file.split(".")[-1]
            im = Image.open("./dataset/data{}".format(i) + file[1:])
            if i < 6:
                im = im.rotate(-90, expand=True)
            im = im.resize((image_width, image_height), Image.ANTIALIAS)
            append_to_dict(im, row)
            append_to_dict(ImageOps.posterize(im, 6), row)
            append_to_dict(ImageOps.expand(ImageOps.crop(im, 10), 10, (255, 255, 255)), row)
            append_to_dict(ImageOps.expand(ImageOps.crop(im, 10), 10), row)
            append_to_dict(ImageOps.autocontrast(im), row)
            if ext != "png":
                blurred = im.filter(ImageFilter.GaussianBlur(1))
                append_to_dict(blurred, row)
                blurred = im.filter(ImageFilter.GaussianBlur(2))
                append_to_dict(blurred, row)

    pd.DataFrame.from_dict(dict_for_csv).to_csv("./dataset_augmented/data{}/labeled_data.csv".format(i), sep="|")
