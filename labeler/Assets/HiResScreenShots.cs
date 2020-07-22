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

    private bool takeHiResShot = false;

    public static string ScreenShotName(int width, int height)
    {
        return string.Format("{0}/screenshots/screen_{1}x{2}_{3}.png",
                             Application.dataPath,
                             width, height,
                             System.DateTime.Now.ToString("yyyy-MM-dd_HH-mm-ss"));
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

    static string getZnacznikPosition(string matName)
    {
        GameObject znacznik = GameObject.Find("Znacznik");
        return string.Format("{0},{1},{2},{3}",
                            matName,
                            znacznik.transform.position,
                            znacznik.transform.eulerAngles,
                            znacznik.transform.localScale
                            );
    }

    string matName;
    public void SaveZnacznikInfo()
    {
        string dataFilePath = string.Format("{0}/screenshots/data/data.txt", Application.dataPath);
        matName = GameObject.Find("Brzuch").GetComponent<Renderer>().material.name;
        int ind = matName.IndexOf(" (");
        matName = matName.Substring(0, ind);
        string line;
        int counter = 1;
        bool found = false;
        string lineToWrite = getZnacznikPosition(matName); // TODO FORMAT idMat,reszta
        using (System.IO.StreamReader file =
           new System.IO.StreamReader(dataFilePath))
        {
            while ((line = file.ReadLine()) != null)
            {
                var list = line.Split(',');//.ToList();
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
            UnityEngine.Debug.Log(counter);
            lineChanger(lineToWrite, dataFilePath, counter);
        }
        else
        {
            File.AppendAllText(dataFilePath, lineToWrite + Environment.NewLine);
        }
    }

    void LateUpdate()
    {
        takeHiResShot |= Input.GetKeyDown("k");
        if (takeHiResShot)
        {
            UnityEngine.Debug.Log(Directory.GetCurrentDirectory());
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
            string filename = ScreenShotName(resWidth, resHeight);
            System.IO.File.WriteAllBytes(filename, bytes);
            SaveZnacznikInfo();
            UnityEngine.Debug.Log(string.Format("Took screenshot to: {0}", filename));
            takeHiResShot = false;
        }
    }
}