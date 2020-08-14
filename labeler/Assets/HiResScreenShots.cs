using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;
using System.IO;
using System.Diagnostics;

public class HiResScreenShots : MonoBehaviour
{
    public int resWidth = 400;
    public int resHeight = 400;
    public int randomPerPhoto = 5;

    private bool takeHiResShot = false;
    private bool randomScreens = false;

    public void RandomZnacznikMove()
    {
        GameObject znacznik = GameObject.Find("Znacznik");
        var rand = new System.Random();

        // scale 0.5 - 0.75
        // x: -0.60 - 0.60 y: -0.75 - 0.75
        // rotation: -50 - 50
        znacznik.transform.position = new Vector3(((float)rand.NextDouble() - 0.5f) * 1.2f, ((float)rand.NextDouble() - 0.5f)* 1.45f, 0);

        var x_angle = rand.Next(50);
        if (rand.NextDouble() > 0.5)
            x_angle = 360 - x_angle;

        var y_angle = rand.Next(50);
        if (rand.NextDouble() > 0.5)
            y_angle = 360 - y_angle;

        znacznik.transform.eulerAngles = new Vector3(x_angle, y_angle, 0);

        var scale = (float)rand.NextDouble() / 4 + 0.5f;
        znacznik.transform.localScale = new Vector3(scale, scale, 0);
    }

    public void TakeRandomScreenshots()
    {
        while (true)
        {
            for (int i = 0; i < randomPerPhoto; i++)
            {
                RandomZnacznikMove();
                randomScreens = true;
                ScreenShot(i);
            }

            bool finish = GameObject.Find("Brzuch").GetComponent<BrzuchScript>().nextBrzuch();
            if (finish)
                return;
        }
    }

    public static string ScreenShotName(int width, int height, int i = 0)
    {
        return string.Format("{0}/screenshots/screen_{1}x{2}_{3}_{4}.png",
                             Application.dataPath,
                             width, height,
                             System.DateTime.Now.ToString("yyyy-MM-dd_HH-mm-ss"),
                             i);
    }

    public void TakeHiResShot()
    {
        takeHiResShot = true;
    }

    static void lineChanger(string newText, string fileName, int line_to_edit)
    {
        // lineChanger("new content for this line" , "sample.text" , 34);
        string[] arrLine = File.ReadAllLines(fileName);
        arrLine[line_to_edit - 1] = newText;
        File.WriteAllLines(fileName, arrLine);
    }

    static string getZnacznikPosition(string matName, string screenName)
    {
        GameObject znacznik = GameObject.Find("Znacznik");
        return string.Format("{0},{1},{2},{3},{5},{4}",
                            matName,
                            znacznik.transform.position,
                            znacznik.transform.eulerAngles,
                            znacznik.transform.localScale,
                            screenName,
                            calculatePoints()
                            );
    }

    static string calculatePoints()
    {
        GameObject znacznik = GameObject.Find("Znacznik");
        var middle = znacznik.transform.position;
        var scale = znacznik.transform.localScale;
        var rotation = znacznik.transform.eulerAngles;

        var width = scale[0];
        var height = scale[1];

        // TO DO zwroc wspolrzedne wierzcholkow jako string czy co chcecie zapisac wraz z obrazem

        return "";
    }

    string matName;
    public void SaveZnacznikInfo(string screenName, bool random)
    {
        string dataFilePath = string.Format("{0}/screenshots/data/data.txt", Application.dataPath);
        matName = GameObject.Find("Brzuch").GetComponent<Renderer>().material.name;
        int ind = matName.IndexOf(" (");
        matName = matName.Substring(0, ind);
        string line;
        int counter = 1;
        string lineToWrite = getZnacznikPosition(matName, screenName);
        if (random)
        {
            File.AppendAllText(dataFilePath, lineToWrite + Environment.NewLine);
        }
        else
        {
            bool found = false;
            using (System.IO.StreamReader file =
               new System.IO.StreamReader(dataFilePath))
            {
                while ((line = file.ReadLine()) != null)
                {
                    var list = line.Split(',');
                    if (list[0] == matName)
                    {
                        found = true;
                        break;
                    }
                    counter++;
                }
            }
            if (found)
            {
                lineChanger(lineToWrite, dataFilePath, counter);
            }
            else
            {
                File.AppendAllText(dataFilePath, lineToWrite + Environment.NewLine);
            }
        }
    }

    public void ScreenShot(int i = 0)
    {
        // UnityEngine.Debug.Log(Directory.GetCurrentDirectory());
        RenderTexture rt = new RenderTexture(resWidth, resHeight, 24);
        GetComponent<Camera>().targetTexture = rt;
        Texture2D screenShot = new Texture2D(resWidth, resHeight, TextureFormat.RGB24, false);
        GetComponent<Camera>().Render();
        RenderTexture.active = rt;
        screenShot.ReadPixels(new Rect(0, 0, resWidth, resHeight), 0, 0);
        GetComponent<Camera>().targetTexture = null;
        RenderTexture.active = null; // JC: added to avoid errors
        Destroy(rt);
        byte[] bytes = screenShot.EncodeToPNG();
        string filename = ScreenShotName(resWidth, resHeight, i);
        System.IO.File.WriteAllBytes(filename, bytes);
        SaveZnacznikInfo(filename, randomScreens);
        UnityEngine.Debug.Log(string.Format("Took screenshot to: {0}", filename));
        randomScreens = false;
        takeHiResShot = false;
    }

    void LateUpdate()
    {
        if (Input.GetKeyDown("x"))
            calculatePoints();

        if (Input.GetKeyDown("p"))
            TakeRandomScreenshots();

        takeHiResShot |= Input.GetKeyDown("k");
        if (takeHiResShot)
        {
            ScreenShot();
        }
    }
}