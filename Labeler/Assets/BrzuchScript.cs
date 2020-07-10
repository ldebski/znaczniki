﻿using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class BrzuchScript : MonoBehaviour
{
    // Start is called before the first frame update
    Object[] Mats;
    GameObject brzuchObject;
    int matsLen;
    int currMat;
    void Start()
    {
        brzuchObject = GameObject.Find("Brzuch");
        Mats = Resources.LoadAll("Mats", typeof(Material));
        matsLen = Mats.Length;
        currMat = 1;
        brzuchObject.GetComponent<Renderer>().material = (Material)Mats[0];
    }

    void resetZnacznik()
    {
        GameObject znacznik = GameObject.Find("Znacznik");
        znacznik.transform.position = Vector3.zero;
        znacznik.transform.eulerAngles = Vector3.zero;
        znacznik.transform.eulerAngles = new Vector3(1.0f, 1.0f, 0.0f);
    }

    // Update is called once per frame
    void Update()
    {
        if (Input.GetKeyDown("space"))
        {
            UnityEngine.Debug.Log(matsLen);
            resetZnacznik();
            brzuchObject.GetComponent<Renderer>().material = (Material)Mats[currMat++];
        }
    }
}
