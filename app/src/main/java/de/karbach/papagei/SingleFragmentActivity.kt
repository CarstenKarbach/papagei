package de.karbach.papagei;

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

abstract class SingleFragmentActivity: AppCompatActivity() {
    /**
     *
     * @return Fragment shown for this activity
     */
    abstract fun createFragment(): Fragment;

    fun getFragmentIfExists(): Fragment?{
        val fm: FragmentManager = this.supportFragmentManager;
        val f:Fragment? = fm.findFragmentById(R.id.fragment_container)
        return f
    }

    /**
     *
     * @return true, if activity should create the up button. False if this is not required.
     */
    public fun showUpButton():Boolean{
        return true;
    }

    protected open fun getLayoutId(): Int{
        return R.layout.single_fragment
    }

    /**
     * create fragment, add it to the fragment_container
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutId());

        val fm: FragmentManager = this.supportFragmentManager;
        var f:Fragment? = fm.findFragmentById(R.id.fragment_container);

        if(f==null){
            f = createFragment();
            fm.beginTransaction().add(R.id.fragment_container, f).commit();
        }
    }
}
