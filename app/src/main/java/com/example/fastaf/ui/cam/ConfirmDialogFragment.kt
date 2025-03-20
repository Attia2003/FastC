package com.example.fastaf.ui.cam

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.fastaf.databinding.DialogconfirmphotoBinding

class ConfirmDialogFragment(
    private val photo: Bitmap,
    private val onConfirm: () -> Unit,
    private val onRetry: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogconfirmphotoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogconfirmphotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.photoPreview.setImageBitmap(photo)


        binding.confirmBtn.setOnClickListener {
            onConfirm()
            dismiss()
        }


        binding.retryBtn.setOnClickListener {
            onRetry()
            dismiss()
        }


        binding.backBtn.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}