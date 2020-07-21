using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class startDetection : MonoBehaviour
{
    public bool firstClick = true;
    public Text textschowed = null;
    public void changeColor()
    {
        if(firstClick){
            textschowed.color = Color.yellow;
            textschowed.text = "DETECTING";
            GameObject.Find("Button").GetComponentInChildren<Text>().text = "Stop";
            firstClick = false;
        }
        else
        {
            firstClick = true;
            textschowed.color = Color.red;
            textschowed.text = "UNDETECTED";
            GameObject.Find("Button").GetComponentInChildren<Text>().text = "Start";
        }
    }
}
