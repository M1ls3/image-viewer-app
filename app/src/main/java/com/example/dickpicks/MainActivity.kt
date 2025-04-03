package com.example.dickpicks

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import java.io.IOException
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var images: List<String>
    private var currentIndex = 0

    private lateinit var gestureDetector: GestureDetector
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)

        // Завантаження зображень з assets/images/
        images = loadImagesFromAssets()

        // Відображаємо перше зображення
        if (images.isNotEmpty()) {
            setImageBitmapFromAssets(images[currentIndex])
        }

        // Налаштовуємо обробку жестів: натискання, свайпи, масштабування
        gestureDetector = GestureDetector(this, GestureListener())
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        imageView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    // Завантаження списку файлів з assets/images/
    private fun loadImagesFromAssets(): List<String> {
        return try {
            val assetManager: AssetManager = assets
            assetManager.list("images")?.toList() ?: emptyList()
        } catch (e: IOException) {
            emptyList()
        }
    }

    // Встановлення зображення без анімації (просто для оновлення контенту)
    private fun setImageBitmapFromAssets(imageName: String) {
        try {
            val inputStream: InputStream = assets.open("images/$imageName")
            val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(bitmap)
            resetScale()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Скидання масштабу до стандартного
    private fun resetScale() {
        scaleFactor = 1.0f
        imageView.scaleX = 1.0f
        imageView.scaleY = 1.0f
    }

    // Обробка свайпу із анімацією
    private fun animateSwipe(direction: Int) {
        // direction: 1 для свайпу вправо (наступна картинка), -1 для свайпу вліво (попередня)
        val width = imageView.width.toFloat()
        val exitTranslation = if (direction == 1) width else -width

        // Анімація виходу поточного зображення
        imageView.animate()
            .translationX(exitTranslation)
            .setDuration(100)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // Оновлюємо індекс зображення циклічно
                    currentIndex = when (direction) {
                        1 -> (currentIndex + 1) % images.size
                        -1 -> if (currentIndex - 1 < 0) images.size - 1 else currentIndex - 1
                        else -> currentIndex
                    }
                    // Завантажуємо нове зображення
                    setImageBitmapFromAssets(images[currentIndex])
                    // Встановлюємо зображення поза екраном для в'їзду
                    imageView.translationX = if (direction == 1) -width else width
                    // Анімація входу зображення до центру
                    imageView.animate()
                        .translationX(0f)
                        .setDuration(100)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .setListener(null)
                        .start()
                }
            }).start()
    }

    // Клас обробки жестів
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            // Натискання показує випадкове зображення з ефектом натискання
            val randomIndex = Random.nextInt(images.size)
            // Анімація натискання схожа на анімацію кнопки
            val shrinkX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 0.9f)
            val shrinkY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 0.9f)
            val expandX = ObjectAnimator.ofFloat(imageView, "scaleX", 0.9f, 1f)
            val expandY = ObjectAnimator.ofFloat(imageView, "scaleY", 0.9f, 1f)
            shrinkX.duration = 100
            shrinkY.duration = 100
            expandX.duration = 100
            expandY.duration = 100
            shrinkX.interpolator = AccelerateDecelerateInterpolator()
            shrinkY.interpolator = AccelerateDecelerateInterpolator()
            expandX.interpolator = AccelerateDecelerateInterpolator()
            expandY.interpolator = AccelerateDecelerateInterpolator()

            shrinkX.start()
            shrinkY.start()
            expandX.start()
            expandY.start()

            currentIndex = randomIndex
            setImageBitmapFromAssets(images[currentIndex])
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null || e2 == null) return false
            val deltaX = e2.x - e1.x
            if (deltaX > 100) {
                // Свайп вправо (наступна картинка)
                animateSwipe(1)
            } else if (deltaX < -100) {
                // Свайп вліво (попередня картинка)
                animateSwipe(-1)
            }
            return true
        }
    }

    // Масштабування зображення
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = max(0.5f, min(scaleFactor, 3.0f))
            imageView.scaleX = scaleFactor
            imageView.scaleY = scaleFactor
            return true
        }
    }
}