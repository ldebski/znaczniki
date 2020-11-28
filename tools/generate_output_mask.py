import os
import pandas as pd
from PIL import Image, ImageFilter, ImageOps, ExifTags, ImageDraw

dict_keys = ("Marker1", "Marker2", "Marker3", "Marker4", "ColorLeft", "ColorRight",
                         "GrayScaleRight", "GreyScaleLeft", "CircleTop", "CircleRight", "CircleBottom", "CircleLeft")

markers = ("Marker1", "Marker2", "Marker3", "Marker4")

image_width = 224
image_height = 224
counter = 0


def generate_mask(im, row):
    mask = Image.new("RGB", (image_width, image_height))
    d = ImageDraw.Draw(mask)
    for label in markers:
        point = eval(row[label])
        d.ellipse((point[0] * image_width - 10, point[1] * image_height - 10, point[0] * image_width + 10, point[1] * image_height + 10), fill='red', outline='red')

    color_left_point = eval(row["ColorLeft"])
    color_right_point = eval(row["ColorRight"])
    grey_scale_left_point = eval(row["GrayScaleRight"])
    grey_scale_right_point = eval(row["GreyScaleLeft"])

    d.line([(color_left_point[0] * image_width, color_left_point[1] * image_height),
            (color_right_point[0] * image_width, color_right_point[1] * image_height)], fill="green", width=4)
    d.line([(grey_scale_left_point[0] * image_width, grey_scale_left_point[1] * image_height),
            (grey_scale_right_point[0] * image_width, grey_scale_right_point[1] * image_height)], fill="blue", width=4)

    # circle_top = eval(row["CircleTop"])
    # circle_right = eval(row["CircleRight"])
    # circle_bottom = eval(row["CircleBottom"])
    # circle_left = eval(row["CircleLeft"])

    # center_horizontal = ((circle_left[0] + circle_right[0]) / 2 * image_width,
    #                      (circle_left[1] + circle_right[1]) / 2 * image_height)
    # center_vertical = ((circle_top[0] + circle_bottom[0]) / 2 * image_width,
    #                    (circle_top[1] + circle_bottom[1]) / 2 * image_height)

    # d.ellipse(((circle_left[0] + circle_left[0]) * image_width, circle_top[1] * image_height,
    #            circle_right[0] * image_width, circle_bottom[1] * image_height), fill='blue', outline='blue')
    mask.filter(ImageFilter.GaussianBlur(100))
    return mask


for i in range(len(os.listdir("./dataset_augmented"))):
    dict_for_csv = {"photoFullPath": [], "Marker1": [], "Marker2": [], "Marker3": [], "Marker4": [],
                    "ColorLeft": [], "ColorRight": [], "GrayScaleRight": [], "GreyScaleLeft": [],
                    "CircleTop": [], "CircleRight": [], "CircleBottom": [], "CircleLeft": []}
    save_x_prefix = "./dataset_mask_output/x/"
    save_y_prefix = "./dataset_mask_output/y/"
    if not os.path.exists(save_x_prefix):
        os.makedirs(save_x_prefix)
    if not os.path.exists(save_y_prefix):
        os.makedirs(save_y_prefix)
    data = pd.read_csv("./dataset_augmented/data{}/labeled_data.csv".format(i), sep="|")
    for index, row in data.iterrows():
        file = row["photoFullPath"]
        im = Image.open("./dataset_augmented/data{}/source_to_label/".format(i) + file)
        im.save(save_x_prefix + str(counter) + ".jpg")
        mask = generate_mask(im, row)
        mask.save(save_y_prefix + str(counter) + ".jpg")
        counter += 1
