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

package com.xsens.dot.android.example.adapters

import XsRecordingFileInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.xsens.dot.android.example.R
import com.xsens.dot.android.example.interfaces.FileSelectionCallback
import com.xsens.dot.android.example.views.RecordingData
import java.util.HashMap

class ExportAdapter(
    private var mDataList: ArrayList<RecordingData>,
    private val mCheckedFileInfoMap: HashMap<String, ArrayList<XsRecordingFileInfo>>,
    private var fileSelectionCallback: FileSelectionCallback
) :
    RecyclerView.Adapter<ExportAdapter.ExportViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExportViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_export, parent, false)
        return ExportViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: ExportViewHolder, position: Int) {
        val data = mDataList[position]
        holder.clItem.setOnClickListener {
            fileSelectionCallback.onFileSelectionClick(data.device.address)
        }
        holder.txtDeviceName.text = data.device.name
        holder.txtDeviceAddress.text = data.device.address
        val selectedFileCount = mCheckedFileInfoMap[data.device.address]?.size ?: 0
        val totalFiles = data.recordingFileInfoList.size
        holder.txtFileCount.text = if (totalFiles == 0) {
            "Please Wait"
        } else {
            "$selectedFileCount/${totalFiles}\nFiles Selected"
        }
    }

    class ExportViewHolder(rootView: View) : ViewHolder(rootView) {
        var clItem: ConstraintLayout = rootView.findViewById(R.id.clItem)
        var txtDeviceName: TextView = rootView.findViewById(R.id.txtDeviceName)
        var txtDeviceAddress: TextView = rootView.findViewById(R.id.txtDeviceAddress)
        var txtFileCount: TextView = rootView.findViewById(R.id.txtFileCount)
    }
}