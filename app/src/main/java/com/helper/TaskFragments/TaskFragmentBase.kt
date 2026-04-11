package com.helper.TaskFragments
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.helper.DataManager.AppSession
import com.helper.DataManager.ClientTaskSession
import com.helper.Logic.BaseTask
import com.helper.Logic.TaskType
import com.helper.TaskFragment
import kotlinx.serialization.json.JsonObject

abstract class TaskFragmentBase<T : ViewBinding> : Fragment() {

    protected lateinit var binding: T
    protected var qst: JsonObject? = null
    protected var _session= AppSession()
    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!initSession()) {
            Log.w("TaskFragmentBase", "Session initialization failed, fragment might not work correctly")
            return
        }
        Log.d("DEBUG", "Task ${this.javaClass.simpleName} is created")
    }


    protected fun initSession(): Boolean {
        val pf = parentFragment as? TaskFragment ?: run {
            Log.d("DEBUG", "initSession: parentFragment is not TaskFragment")
            return false
        }

        _session = pf.getClientSession().value ?: run {
            Log.d("DEBUG", "initSession: current session is null")
            return false
        }

        if (!isSessionValid(_session)) {
            Log.d("DEBUG", "initSession: invalid session")
            return false
        }

        Log.i("DEBUG", "initSession: session initialized successfully")
        return true
    }

    abstract fun checkButtonIsPressed()
    abstract fun clearButtonIsPressed()
    abstract fun loadFragmentDataFromJSON()
    abstract fun loadFragmentFromSession(bt: BaseTask)
    abstract fun compareAnswers()


    abstract fun parsedAnswer()
    abstract fun parsedTemplate()
    abstract fun parsedBlank()
    abstract fun parsedOptions()
    abstract fun showItem()

    abstract fun collectClientAnswer()
    abstract fun createBaseTask():BaseTask
    abstract fun updateClientTaskSession(bt: BaseTask): BaseTask
    open fun saveClientTaskSession(): BaseTask? {

        val taskType = TaskType.valueOf(_session.taskType!!)
        val taskId = _session.taskID.toString()

        // Получаем существующее задание или создаём новое
        val task = ClientTaskSession.getTaskByTypeAndId(taskType, taskId)
            ?: createBaseTask().also {
                ClientTaskSession.addTask(it)
                Log.d("TASKINFO", "Task created: ${it.id}")
            }

        // обновляем сессию
        updateClientTaskSession(task)

        val resultTask: BaseTask = task

        Log.d("TASKINFO", "ClientTaskSession count=${ClientTaskSession.getTaskCount()}")

        return resultTask
    }


    open fun isSessionValid(session: AppSession): Boolean {
        // Локальные переменные с "_" перед именем
        val _taskType = session.taskType
        val _classID = session.classID
        val _language = session.language
        val _topic = session.topic
        val _taskID = session.taskID

        // Логируем все поля
        Log.d("DEBUG", "Session check: _taskType=$_taskType, _classID=$_classID, _language=$_language, _topic=$_topic, _taskID=$_taskID")

        // Проверяем каждое поле
        val _taskTypeValid = _taskType != null
        val _classIDValid = _classID != null
        val _languageValid = _language != null
        val _topicValid = _topic != null
        val _taskIDValid = _taskID != null
        val _taskTypeExists = _taskType?.let { type ->
            enumValues<TaskType>().any { it.name == type }
        } ?: false

        // Логируем результаты проверки
        Log.d("DEBUG", "Field validity: _taskType=$_taskTypeValid, _classID=$_classIDValid, _language=$_languageValid, _topic=$_topicValid, _taskID=$_taskIDValid, _taskTypeExists=$_taskTypeExists")

        return _taskTypeValid && _classIDValid && _languageValid && _topicValid && _taskIDValid && _taskTypeExists
    }

    final override fun onDestroy() {
        super.onDestroy()
        collectClientAnswer()
        val _cts=saveClientTaskSession()
        ClientTaskSession.findByBaseTask(_cts!!)?.log()
        Log.d("DEBUG", "Task ${this.javaClass.simpleName} is destroyed")
    }
}