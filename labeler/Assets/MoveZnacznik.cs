using System.Collections;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Diagnostics;
using System.Security.Cryptography;
using UnityEngine;

public class MoveZnacznik : MonoBehaviour
{
    Vector3 mousePos;
    bool mouseDown = false;
    bool mouseRotateDown = false;
    // Start is called before the first frame update
    void Start()
    {
        
    }

    void OnMouseDown()
    {
        mouseDown = true;
        mousePos = Camera.main.ScreenToWorldPoint(Input.mousePosition);
    }

    bool MouseMove()
    {
        return (Input.GetAxis("Mouse X") != 0) || (Input.GetAxis("Mouse Y") != 0);
    }

    // Update is called once per frame
    void Update()
    {
        float val = 0.005f;
        // position
        if (Input.GetKey(KeyCode.UpArrow) && transform.position.y < 1.0f)
        {
            transform.position += Vector3.up * val;
        }
        if (Input.GetKey(KeyCode.DownArrow) && transform.position.y > -1.0f)
        {
            transform.position -= Vector3.up * val;
        }
        if (Input.GetKey(KeyCode.RightArrow) && transform.position.x < 1.0f)
        {
            transform.position += Vector3.right * val;
        }
        if (Input.GetKey(KeyCode.LeftArrow) && transform.position.x > -1.0f)
        {
            transform.position -= Vector3.right * val;
        }
        // rotations
        if (Input.GetKey("w") && (transform.eulerAngles.x > 300.0f || transform.eulerAngles.x < 70.0f))
        {
            transform.eulerAngles -= Vector3.right * val * 100;
        }
        if (Input.GetKey("s") && (transform.eulerAngles.x > 290.0f || transform.eulerAngles.x < 60.0f))
        {
            transform.eulerAngles += Vector3.right * val * 100;
        }
        if (Input.GetKey("a") && (transform.eulerAngles.y > 300.0f || transform.eulerAngles.y < 70.0f))
        {
            transform.eulerAngles -= Vector3.up * val*100;
        }
        if (Input.GetKey("d") && (transform.eulerAngles.y > 290.0f || transform.eulerAngles.y < 60.0f))
        {
            transform.eulerAngles += Vector3.up * val*100;
        }
        // mouse wheel for scale
        if (Input.mouseScrollDelta.y == 1 && transform.localScale.x < 1.0f)
        {
            transform.localScale += (Vector3.up * val * 5 + Vector3.right * val * 5);
        }
        if (Input.mouseScrollDelta.y == -1 && transform.localScale.x > 0.3f)
        {
            transform.localScale -= (Vector3.up * val * 5 + Vector3.right * val * 5);
        }

        // move with mouse
        if (Input.GetMouseButtonUp(0))
        {
            mouseDown = false;
        }
        if (mouseDown && MouseMove())
        {
            Vector3 currMousePos = Camera.main.ScreenToWorldPoint(Input.mousePosition);
            transform.position += (currMousePos - mousePos);
            mousePos = currMousePos;
        }

        // rotate with mouse
        if (Input.GetMouseButtonDown(1))
        {
            mousePos = Camera.main.ScreenToWorldPoint(Input.mousePosition);
            mouseRotateDown = true;
        }
        if (Input.GetMouseButtonUp(1))
        {
            mousePos = Camera.main.ScreenToWorldPoint(Input.mousePosition);
            mouseRotateDown = false;
        }
        if (mouseRotateDown && MouseMove())
        {
            Vector3 currMousePos = Camera.main.ScreenToWorldPoint(Input.mousePosition);
            Vector3 rotateVec = (currMousePos - mousePos);
            Vector3 rotateVecFixed = new Vector3(rotateVec.y, rotateVec.x, 0);
            transform.eulerAngles += rotateVecFixed * 70;
            if (transform.eulerAngles.x > 60.0f && transform.eulerAngles.x < 280)
            {
                transform.eulerAngles = new Vector3(60.0f, transform.eulerAngles.y, 0);
            }
            if (transform.eulerAngles.x < 300.0f && transform.eulerAngles.x > 80)
            {
                transform.eulerAngles = new Vector3(300.0f, transform.eulerAngles.y, 0);
            }
            if (transform.eulerAngles.y > 60.0f && transform.eulerAngles.y < 280)
            {
                transform.eulerAngles = new Vector3(transform.eulerAngles.x, 60.0f, 0);
            }
            if (transform.eulerAngles.y < 300.0f && transform.eulerAngles.y > 80)
            {
                transform.eulerAngles = new Vector3(transform.eulerAngles.x, 300.0f, 0);
            }
            mousePos = currMousePos;
        }

    }
}
