package com.bedu.terceros

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bedu.terceros.databinding.ActivityFacebookBinding
import com.facebook.share.model.ShareHashtag
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog


class FacebookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFacebookBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFacebookBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnLink.setOnClickListener {
            val content = ShareLinkContent.Builder()
                .setContentUrl(Uri.parse("https://bedu.org/"))
                .setQuote("Logra + con BEDU")
                .setShareHashtag(
                    ShareHashtag.Builder()
                        .setHashtag("#RetaTuPotencial")
                        .build()
                )
                .build()

            ShareDialog.show(this, content)
        }
        binding.btnPicture.setOnClickListener {
            val image = BitmapFactory.decodeResource(resources, R.drawable.bedu)
            val photo = SharePhoto.Builder()
                .setBitmap(image)
                .build()
            val photoContent = SharePhotoContent.Builder()
                .addPhoto(photo)
                .build()

            ShareDialog.show(this, photoContent)
        }
    }
}