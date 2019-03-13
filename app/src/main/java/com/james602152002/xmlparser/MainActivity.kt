package com.james602152002.xmlparser

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import android.util.Log
import android.util.Xml
import android.view.View

import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val builder = StringBuilder()
    //    @BindView(R.id.saury)
    //    AppCompatButton saury;
    //    @BindView(R.id.saury_jp)
    //    AppCompatButton sauryJp;
    //    @BindView(R.id.saury_kr)
    //    AppCompatButton sauryKr;
    //    @BindView(R.id.saury_cn)
    //    AppCompatButton sauryCn;
    //    @BindView(R.id.saury_tw)
    //    AppCompatButton sauryTw;

//    private val culture: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parseXML(`is`: InputStream) {
        builder.delete(0, builder.length)
        builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + "<resources xmlns:tools=\"http://schemas.android.com/tools\" tools:ignore=\"MissingTranslation\">\n\n")
        //        List<Student> list = null;
        //        Student student = null;
        //创建xmlPull解析器
        val parser = Xml.newPullParser()
        ///初始化xmlPull解析器
        parser.setInput(`is`, "utf-8")
        //读取文件的类型
        var type = parser.eventType
        //无限判断文件类型进行读取
        while (type != XmlPullParser.END_DOCUMENT) {
            when (type) {
                //开始标签
                XmlPullParser.START_TAG -> if ("text" == parser.name) {
                    //                        <Data><![CDATA[：%s]]></Data>
                    var name = parser.getAttributeValue(null, "name")
                    var value: String? = parser.getAttributeValue(null, "value")
                    value = value?.replace("&".toRegex(), "&amp;")?.replace("'".toRegex(), "\\\\'")?.replace("忘记密码？ \\{0\\}\\.".toRegex(), "忘记密码？")?.replace("\\{0\\}\\.".toRegex(), "%s")
                            ?: ""
                    //                                .replaceAll("\\{0\\}", "%s");
                    if (value.contains("{0}")) {
                        if (isNoColorKey(name)) {
                            value = value.replace("\\{0\\}".toRegex(), "%s")
                        } else {
                            value = "<Data><![CDATA[" + value.substring(0, value.indexOf("{0}")) + "<font color=\"#5D73FA\">%s</font>" +
                                    value.substring(value.indexOf("{0}") + "{0}".length, value.length) +
                                    "]]></Data>"
                        }
                    }
                    //                        value = value.replaceAll("'", "&apos;");
                    name = name.replace("\\(".toRegex(), "").replace("\\)".toRegex(), "")
                    name = name.replace(".", "_")
                    builder.append("    <string name=\"").append(name.trim { it <= ' ' }).append("\">")
                            .append(value).append("</string>\n")
                }
                //结束标签
                XmlPullParser.END_TAG -> if ("text" == parser.name) {
                    //                        list.add(student);
                }
            }//                    else if ("localizationDictionary".equals(parser.getName())) {
            //                        culture = parser.getAttributeValue(null, "culture");
            //                    }
            //继续往下读取标签类型
            type = parser.next()
        }
        builder.append("\n</resources>")
    }

    private fun isNoColorKey(key: String): Boolean {
        return when (key) {
            "ConflictCounts", "RecordsCount", "DeadlineInHoursCnt",
            "DeadlineDate", "DeadlineInDaysCnt", "RemindAheadOfDate",
            "DocCounts", "MinutesAgo", "UploadedDocCnt", "ApprovedByAuditorCnt",
            "SealCnt", "DaysCnt", "WorkLogParticipantsHint", "ConflictRecordsCnt",
            "RemainingAnnualLeaveDays", "RemindBeforeMinutes", "RemindBeforeHours",
            "RemindBeforeDays", "RemindBeforeWeeks", "PreviewTemplate", "SelectableLogCnt",
            "OptionalContractInfoCnt", "OptionalFeeCnt", "SelectedCnt", "LawyerCnt",
            "ContactsCnt" -> true
            else -> false
        }
    }

    override fun onClick(v: View) {
        AndPermission.with(this).permission(Permission.WRITE_EXTERNAL_STORAGE).onGranted {
            try {
                parseXML(resources.assets.open((v as AppCompatButton).text.toString()))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val result = builder.toString()
            Log.i("", "result ======= $result")
            var path = Environment.getExternalStorageDirectory().absolutePath + File.separator + "values"
            when (v.id) {
                R.id.saury_jp -> {
                    path = "$path-ja-rJP"
                    createXmlFile(path, result)
                }
                R.id.saury_kr -> {
                    path = "$path-ko-rKR"
                    createXmlFile(path, result)
                }
                R.id.saury_cn -> {
                    createXmlFile("$path-zh", result)
                    createXmlFile("$path-zh-rCN", result)
                }
                R.id.saury_tw -> {
                    path = "$path-zh-rTW"
                    createXmlFile(path, result)
                }
                R.id.saury -> {
                    createXmlFile(path, result)
                    createXmlFile("$path-en", result)
                }
            }
        }.start()
    }

    private fun createXmlFile(path: String, data: String) {
        var path = path
        // Create the folder.
        path += File.separator
        val fPath = File(path)
        if (!fPath.exists()) {
            if (!fPath.mkdirs()) {
            }
        }
        fPath.setReadable(true)
        fPath.setWritable(true)
        // Create the file.
        val file = File(fPath, "strings.xml")
        try {
            val fop = FileOutputStream(file)
            if (!file.exists())
                file.createNewFile()
            val contentInBytes = data.toByteArray()// 取的字串內容bytes

            fop.write(contentInBytes) //輸出
            fop.flush()
            fop.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: $e")
        }

    }
}
