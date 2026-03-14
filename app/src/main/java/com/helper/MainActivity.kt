package com.helper

import android.app.Activity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.helper.DataManager.AppSession
import com.helper.DataManager.DataLC
import com.helper.TaskFragments.VideoFragment
import com.helper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val dataLC: DataLC by viewModels()
    lateinit var binding: ActivityMainBinding

    private val currentSession=AppSession()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }


        binding.btnBack.setOnClickListener {
            println("HELLO")
            supportFragmentManager.popBackStack()

        }
        if (savedInstanceState == null) {
            OpenFragment(HelloFragment.newInstance(), R.id.content_container, false)
        }


        //OpenFragment(HelloFragment.newInstance(),R.id.frame_conteiner)
        //OpenFragment(MainFragment.newInstance(),R.id.frame_conteiner2)
        //OpenFragment(ClientFragment.newInstance(),R.id.frame_conteiner)
        //OpenFragment(HelloFragment.newInstance(),R.id.content_container,false)
        //OpenFragment(TaskFragment.newInstance(TaskType.SENTENCE),R.id.content_container)
        //OpenFragment(VideoFragment.newInstance(),R.id.content_container)


        dataLC.currentSession.observe(this) { session ->
            println("ClassID=${session.classID},TopicID=${session.topic}, TaskID=${session.taskID}, Language=${session.language}")
        }
        dataLC.currentClient.observe(this){
            println("Name=${it.name}, sName=${it.sName}, Activity=${it.activity}")
        }
    }



    fun OpenFragment(
        frg: Fragment,
        idHolder: Int,
        addToBackStack: Boolean = true,
        backStackTag: String? = null,
        clearUpToTag: String? = null
    ) {
        if (clearUpToTag != null) {
            supportFragmentManager.popBackStack(clearUpToTag, 0)
        }

        val transaction = supportFragmentManager
            .beginTransaction()
            .replace(idHolder, frg)

        if (addToBackStack) {
            transaction.addToBackStack(backStackTag ?: frg::class.java.simpleName)
        }

        transaction.commit()
    }
    fun saveCurrentFragment(bundle: Bundle) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.content_container)
        currentFragment?.let {
            supportFragmentManager.putFragment(bundle, "current_fragment", it)
        }
    }
}
