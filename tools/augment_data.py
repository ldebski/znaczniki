import os
import random
import math
import pandas as pd
from PIL import Image, ImageFilter


def append_to_dict(im, row):
    im.save(save_dir_prefix.format(directory_index) + str(counter[directory_index]) + "." + ext)
    dict_for_csv["photoFullPath"].append(str(counter[directory_index]) + ".jpg")
    dict_for_csv["LeftTop"].append(row["LeftTop"])
    dict_for_csv["RightTop"].append(row["RightTop"])
    dict_for_csv["RightLow"].append(row["RightLow"])
    dict_for_csv["LeftLow"].append(row["LeftLow"])
    counter[directory_index] += 1


dict_for_csv = {"photoFullPath": [], "LeftTop": [], "RightTop": [], "RightLow": [], "LeftLow": []}
save_dir_prefix = "./renamed_photos{}/"
counter = [0, 0, 0, 0]
data = pd.read_csv("labeled_data.csv", sep="|", usecols=["photoFullPath", "LeftTop", "RightTop", "RightLow", "LeftLow"])
for index, row in data.iterrows():
    file = row["photoFullPath"]
    for degree_mul in range(1):
        directory_index = math.floor(random.random() * 1)
        if not os.path.exists(save_dir_prefix.format(directory_index)):
            os.makedirs(save_dir_prefix.format(directory_index))
        ext = file.split(".")[-1]
        im = Image.open(file)
        im = im.resize((200, 300), Image.ANTIALIAS)
        append_to_dict(im, row)

        gray = im.convert('L')
        append_to_dict(gray, row)

        if ext != "png":
            blurred = im.filter(ImageFilter.GaussianBlur(2))
            append_to_dict(blurred, row)

pd.DataFrame.from_dict(dict_for_csv).to_csv("./augmented_data.csv", sep="|")
