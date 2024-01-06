package com.example.ActivityRecognition

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment


class MyDialog : DialogFragment(), DialogInterface.OnClickListener{
    companion object{
        const val DIALOG_KEY = "dialog"
        const val DURATION_DIALOG = 2
        const val DISTANCE_DIALOG = 3
        const val CALORIES_DIALOG = 4
        const val HEARTRATE_DIALOG = 5
        const val COMMENT_DIALOG = 6
    }

    lateinit var duration_input: EditText
    lateinit var distance_input: EditText
    lateinit var calories_input: EditText
    lateinit var heartrate_input: EditText
    lateinit var comment_input: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        lateinit var ret: Dialog
        val bundle = arguments
        val dialogId = bundle?.getInt(DIALOG_KEY)
        if (dialogId == DURATION_DIALOG) {
            val builder = AlertDialog.Builder(requireActivity())
            val view: View = requireActivity().layoutInflater.inflate(R.layout.duration_dialog, null)
            duration_input = view.findViewById(R.id.duration_dialog)
//            println("debug::" + input.text)
            builder.setView(view)
            builder.setTitle("Duration")
//            builder.setPositiveButton("ok", this)
            // about how to override setPositiveButton using lambda
            // reference: https://blog.csdn.net/cunchi4221/article/details/107475496?ops_request_misc=%257B%2522request%255Fid%2522%253A%2522166719566016782395355359%2522%252C%2522scm%2522%253A%252220140713.130102334..%2522%257D&request_id=166719566016782395355359&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~all~baidu_landing_v2~default-2-107475496-null-null.142^v62^pc_rank_34_queryrelevant25,201^v3^control_1,213^v1^control&utm_term=setPositiveButton%20kotlin&spm=1018.2226.3001.4187
            builder.setPositiveButton("ok") { _,_ ->
                // when click ok, save the input data to bundle,
                // and pass it to ManualEntryActivity
                // for storing to database
                println("dia::" + duration_input.text.toString() + ".")
                // this if check is for condition that user open the dialog but did not enter data
                if(duration_input.text.toString()!="") {
                    bundle.putString("input_duration", duration_input.text.toString())
                }
                Toast.makeText(activity, "ok-clicked", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("cancel", this)
            ret = builder.create()
        }
        else if (dialogId == DISTANCE_DIALOG) {
            val builder = AlertDialog.Builder(requireActivity())
            val view: View = requireActivity().layoutInflater.inflate(R.layout.distance_dialog, null)
            distance_input = view.findViewById(R.id.distance_dialog)
            builder.setView(view)
            builder.setTitle("Distance")
            builder.setPositiveButton("ok") { _,_ ->
                if(distance_input.text.toString()!="") {
                    bundle.putString("input_distance", distance_input.text.toString())
                }
                Toast.makeText(activity, "ok-clicked", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("cancel", this)
            ret = builder.create()
        }
        else if (dialogId == CALORIES_DIALOG) {
            val builder = AlertDialog.Builder(requireActivity())
            val view: View = requireActivity().layoutInflater.inflate(R.layout.calories_dialog, null)
            calories_input = view.findViewById(R.id.calories_dialog)
            builder.setView(view)
            builder.setTitle("Calories")
            builder.setPositiveButton("ok") { _,_ ->
                if(calories_input.text.toString()!="") {
                    bundle.putString("input_calories", calories_input.text.toString())
                }
                Toast.makeText(activity, "ok-clicked", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("cancel", this)
            ret = builder.create()
        }
        else if (dialogId == HEARTRATE_DIALOG) {
            val builder = AlertDialog.Builder(requireActivity())
            val view: View = requireActivity().layoutInflater.inflate(R.layout.heartrate_dialog, null)
            heartrate_input = view.findViewById(R.id.heartrate_dialog)
            builder.setView(view)
            builder.setTitle("Heart Rate")
            builder.setPositiveButton("ok") { _,_ ->
                if(heartrate_input.text.toString()!="") {
                    bundle.putString("input_heartrate", heartrate_input.text.toString())
                }
                Toast.makeText(activity, "ok-clicked", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("cancel", this)
            ret = builder.create()
        }
        else if (dialogId == COMMENT_DIALOG) {
            val builder = AlertDialog.Builder(requireActivity())
            val view: View = requireActivity().layoutInflater.inflate(R.layout.comment_dialog, null)
            comment_input = view.findViewById(R.id.comment_dialog)
            builder.setView(view)
            builder.setTitle("Comment")
            builder.setPositiveButton("ok") { _,_ ->
                if(comment_input.text.toString()!="") {
                    bundle.putString("input_comment", comment_input.text.toString())
                }
                Toast.makeText(activity, "ok-clicked", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("cancel", this)
            ret = builder.create()
        }
        // the data above are pass in String type
        return ret
    }

    // a little duplicate here
    override fun onClick(dialog: DialogInterface, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE) {
            Toast.makeText(activity, "ok-clicked", Toast.LENGTH_SHORT).show()
        } else if (item == DialogInterface.BUTTON_NEGATIVE) {
            Toast.makeText(activity, "cancel-clicked", Toast.LENGTH_SHORT).show()
        }
    }
}