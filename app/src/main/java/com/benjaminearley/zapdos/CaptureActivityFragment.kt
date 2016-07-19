package com.benjaminearley.zapdos

import android.support.v4.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.benjaminearley.zapdos.util.Pokemon
import kotlinx.android.synthetic.main.fragment_capture.*

class CaptureActivityFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_capture, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pokemonNames = Pokemon.names.keys

        val adapter = ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, pokemonNames.toList())

        pokemonSearch?.validator = Validator(pokemonNames)
        pokemonSearch?.addTextChangedListener(TextWatcher())
        pokemonSearch?.setAdapter(adapter)
        pokemonSearch?.threshold = 1

        pokemonSearch?.setOnEditorActionListener({ textView, id, keyEvent ->
            if (id == R.id.submit || id == EditorInfo.IME_NULL) {
                if (pokemonNames.any { name -> name.toLowerCase().equals((textView as? AutoCompleteTextView)?.text?.toString()?.toLowerCase()?:"") }) {
                    Toast.makeText(activity, "Submit " + textView.text, Toast.LENGTH_LONG).show()
                    true
                } else false
            } else false
        })

    }

    internal inner class Validator(pokemonNames: MutableSet<String>) : AutoCompleteTextView.Validator {

        val names = pokemonNames

        override fun isValid(text: CharSequence): Boolean {
            if (names.any { name -> name.toLowerCase().contains(text.toString().toLowerCase()) }) {
                return true
            }

            return false
        }

        override fun fixText(invalidText: CharSequence): CharSequence {
            return ""
        }
    }

    internal  inner class TextWatcher : android.text.TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            pokemonSearch?.performValidation()
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

    }
}
