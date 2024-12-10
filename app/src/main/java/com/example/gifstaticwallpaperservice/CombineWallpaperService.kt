package com.example.gifstaticwallpaperservice

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Movie
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.io.IOException

class CombinedWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine? {
        return try {
            val movie =
                Movie.decodeStream(resources.openRawResource(R.raw.moon))  // GIF resource
            GIFWallpaper(movie)
        } catch (e: IOException) {
            null
        }
    }

    private inner class GIFWallpaper(private val movie: Movie) : Engine() {

        private val frameDuration = 20
        private var holder: SurfaceHolder? = null
        private val handler = Handler()
        private val bitmap: Bitmap =
            BitmapFactory.decodeResource(resources, R.drawable.pink_panther ) // Static image


        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            holder = surfaceHolder
        }

        private val drawGIF = Runnable { draw() }

        private fun draw() {

                val canvas = holder?.lockCanvas()
                canvas?.let {
                    // Always draw the static image first
                    val offsetX = (canvas.width - bitmap.width) / 2f
                    it.drawBitmap(bitmap, offsetX, 0f, null)
                    it.save()

                    val sizeMin = (it.height / 6f)
                    val sizeMax = (it.height / 4f)

                    val sizeOfGif = adjustSizeValue(
                        sizeMin.toInt(),
                        sizeMax.toInt(),
                        progress = 0   // add seekbar/slider

                    ).toFloat()

                    val width = sizeOfGif.toInt()
                    val height = sizeOfGif.toInt()
                    val scaledBitmap =
                        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

                    val tempCanvas = Canvas(scaledBitmap)
                    val scaleX = width.toFloat() / movie.width()
                    val scaleY = height.toFloat() / movie.height()
                    tempCanvas.scale(scaleX, scaleY)
                    movie.draw(tempCanvas, 0f, 0f)

                        it.drawBitmap(scaledBitmap, 300f, 350f, null)

                    scaledBitmap.recycle()
                    it.restore()

                    holder?.unlockCanvasAndPost(it)

                    // Update the movie time to the current system time
                    movie.setTime((System.currentTimeMillis() % movie.duration()).toInt())
                    handler.removeCallbacks(drawGIF)
                    handler.postDelayed(drawGIF, frameDuration.toLong())
                }
            }


        override fun onVisibilityChanged(visible: Boolean) {
                draw()  // draw the static image
                handler.post(drawGIF)
        }


        fun adjustSizeValue(min: Int, max: Int, progress: Int): Int {
            // progress = adjust using seekbar/slider
            return min + ((progress / 100.0) * (max - min)).toInt()
        }

        override fun onDestroy() {
            super.onDestroy()
            handler.removeCallbacks(drawGIF)
        }
    }
}
