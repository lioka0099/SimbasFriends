package com.example.simbasfriends.activities.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.example.simbasfriends.R

class GalleryAdapter(
    private val context: Context,
    private val imageUrls: List<String>,
    private val isCurrentUserProfile: Boolean,
    private val onAddPhotoClickedListener: () -> Unit,
    private val onDeletePhotoClicked: (String) -> Unit
): BaseAdapter() {
    override fun getCount(): Int = imageUrls.size + 1

    override fun getItem(position: Int): Any? = if (position == 0) null else imageUrls[position - 1]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val imageView: AppCompatImageView

        if (isCurrentUserProfile && position == 0) {
            // Show "Add Photo" button only for the current user's profile
            view = View.inflate(context, R.layout.item_add_photo, null)
            imageView = view.findViewById(R.id.addPhotoImageView)
            imageView.setOnClickListener { onAddPhotoClickedListener() }
        } else {
            // Normal photo item
            view = View.inflate(context, R.layout.item_gallery_image, null)
            imageView = view.findViewById(R.id.galleryImageView)

            val imageIndex = position - if (isCurrentUserProfile) 1 else 0
            if (imageIndex in imageUrls.indices) {
                Glide.with(context)
                    .load(imageUrls[imageIndex])
                    .centerCrop()
                    .into(imageView)

                // Prevent delete dialog for other users' profiles
                if (isCurrentUserProfile) {
                    imageView.setOnLongClickListener {
                        showDeleteConfirmationDialog(imageUrls[imageIndex])
                        true
                    }
                } else {
                    imageView.setOnLongClickListener(null) // Disable long press for other users' profiles
                }
            }
        }
        return view
    }
    // Show delete confirmation dialog
    private fun showDeleteConfirmationDialog(imageUrl: String) {
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Delete Photo")
            .setMessage("Are you sure you want to delete this photo?")
            .setPositiveButton("Yes") { _, _ -> onDeletePhotoClicked(imageUrl) }
            .setNegativeButton("No", null)
            .create()
        alertDialog.show()
    }
}
