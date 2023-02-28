/*
 * Copyright (c) 2003-2020 Movella Technologies B.V. or subsidiaries worldwide.
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *   1.      Redistributions of source code must retain the above copyright notice,
 *            this list of conditions, and the following disclaimer.
 *
 *   2.      Redistributions in binary form must reproduce the above copyright notice,
 *            this list of conditions, and the following disclaimer in the documentation
 *            and/or other materials provided with the distribution.
 *
 *   3.      Neither the names of the copyright holders nor the names of their contributors
 *            may be used to endorse or promote products derived from this software without
 *            specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 *   EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 *   THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 *   OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *   HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY OR
 *   TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.THE LAWS OF THE NETHERLANDS
 *   SHALL BE EXCLUSIVELY APPLICABLE AND ANY DISPUTES SHALL BE FINALLY SETTLED UNDER THE RULES
 *   OF ARBITRATION OF THE INTERNATIONAL CHAMBER OF COMMERCE IN THE HAGUE BY ONE OR MORE
 *   ARBITRATORS APPOINTED IN ACCORDANCE WITH SAID RULES.
 */

package com.xsens.dot.android.example.views

import RecordingFileSelectionAdapter
import XsRecordingFileInfo
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xsens.dot.android.example.BuildConfig
import com.xsens.dot.android.example.R
import com.xsens.dot.android.example.databinding.ActivityRecordingFileSelectionBinding

class RecordingFileSelectionActivity : AppCompatActivity(), View.OnClickListener, RecordingFileSelectionAdapter.RecordingFileSelectionCallback {

    companion object {
        const val KEY_SELECT_FILE_LIST_RESULT = "select_file_list"
        const val KEY_TITLE = "key_title"
        const val KEY_ADDRESS = "key_address"
        const val KEY_FILE_INFO_LIST = "file_info_list"
        const val KEY_CHECKED_FILE_INFO_LIST = "checked_file_info_list"
    }

    private lateinit var mBinding: ActivityRecordingFileSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        mBinding = ActivityRecordingFileSelectionBinding.inflate(LayoutInflater.from(this))
        setContentView(mBinding.root)
        initView()
    }

    private fun initView() {
        mBinding.title.text = "Export"

        val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(KEY_FILE_INFO_LIST, XsRecordingFileInfo::class.java)
        } else {
            intent.getParcelableArrayListExtra<XsRecordingFileInfo>(KEY_FILE_INFO_LIST)
        }
        var checkedList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(KEY_CHECKED_FILE_INFO_LIST, XsRecordingFileInfo::class.java)
        } else {
            intent.getParcelableArrayListExtra<XsRecordingFileInfo>(KEY_CHECKED_FILE_INFO_LIST)
        }
        checkedList = checkedList ?: ArrayList()

        updateFileSelectedString(checkedList.size)

        mBinding.textViewCancel.setOnClickListener(this)
        mBinding.textViewConfirm.setOnClickListener(this)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        mBinding.listViewDevice.layoutManager = layoutManager
        mBinding.listViewDevice.itemAnimator = DefaultItemAnimator()

        list?.let {
            mBinding.listViewDevice.adapter = RecordingFileSelectionAdapter(it, checkedList, this)
        }

    }

    private fun updateFileSelectedString(checkedCount: Int) {
        val fileSelectedString = if (checkedCount > 1) R.string.n_files_selected else R.string.n_file_selected
        mBinding.nFileSelected.text = getString(fileSelectedString, checkedCount)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.textView_cancel) {
            setResult(Activity.RESULT_CANCELED)
            finish()

            return
        } else if (v.id == R.id.textView_confirm) {
            val address = intent.getStringExtra(KEY_ADDRESS)

            val adapter = (mBinding.listViewDevice.adapter as RecordingFileSelectionAdapter)
            val selectList = adapter.mCheckedDeviceList

            val intent = Intent()
            intent.putExtra(KEY_ADDRESS, address)
            intent.putExtra(KEY_SELECT_FILE_LIST_RESULT, selectList)
            setResult(Activity.RESULT_OK, intent)
            finish()

            return
        }

        setResult(Activity.RESULT_CANCELED)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        setResult(Activity.RESULT_CANCELED)
    }

    override fun onFileSelectionUpdate(selectedCount: Int) {
        updateFileSelectedString(selectedCount)
    }
}