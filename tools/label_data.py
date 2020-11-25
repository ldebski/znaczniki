import cv2
import os
import pandas as pd

counter_to_key_mapper = ("Marker1", "Marker2", "Marker3", "Marker4", "ColorLeft", "ColorRight",
                         "GrayScaleRight", "GreyScaleLeft", "CircleTop", "CircleRight", "CircleBottom", "CircleLeft")
dict_for_csv = {"photoFullPath": [], "Marker1": [], "Marker2": [], "Marker3": [], "Marker4": [],
                "ColorLeft": [], "ColorRight": [], "GrayScaleRight": [], "GreyScaleLeft": [],
                "CircleTop": [], "CircleRight": [], "CircleBottom": [], "CircleLeft": []}


def get_image_name():
    directory = "./source_to_label"
    directory_files = os.listdir(directory)
    if "desktop.ini" in directory_files:
        directory_files.remove("desktop.ini")
    for img_name in directory_files:
        full_img_name = "{}/{}".format(directory, img_name)
        dict_for_csv["photoFullPath"].append(full_img_name)
        yield full_img_name


def click_event(event, x, y, flags, params):
    height, width, _ = params["img_shape"]

    if event == cv2.EVENT_LBUTTONDOWN:
        print(x/width, ' ', y/height)
        dict_for_csv[counter_to_key_mapper[params["counter"] % 12]].append("({}, {})".format(x/width, y/height))
        params["counter"] += 1
        if params["counter"] % 12 == 0:
            try:
                img_name = next(image_names_generator)
                print(img_name)
            except StopIteration:
                pd.DataFrame.from_dict(dict_for_csv).to_csv("./labeled_data.csv", sep="|")
                cv2.destroyAllWindows()
                return

            img = cv2.imread(img_name, 1)
            params["img_shape"] = img.shape
            cv2.imshow('image', img)


if __name__ == "__main__":
    image_names_generator = get_image_name()
    img_name = next(image_names_generator)
    img = cv2.imread(img_name, 1)
    cv2.imshow('image', img)
    cv2.setMouseCallback('image', click_event, param={"image_names_generator": image_names_generator,
                                                      "img_shape": img.shape, "counter": 0})

    cv2.waitKey(0)
