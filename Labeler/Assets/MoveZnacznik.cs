using System.Collections;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Diagnostics;
using System.Security.Cryptography;
using UnityEngine;

public class MoveZnacznik : MonoBehaviour
{
    // Start is called before the first frame update
    void Start()
    {
        
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
    }
}
