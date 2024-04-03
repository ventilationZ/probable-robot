package jcornaz.demo.kotlin.flappybird.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.FillViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import jcornaz.demo.kotlin.flappybird.WORLD_HEIGHT
import jcornaz.demo.kotlin.flappybird.WORLD_WIDTH
import jcornaz.demo.kotlin.flappybird.actor.Bird
import jcornaz.demo.kotlin.flappybird.actor.floorCycle
import jcornaz.demo.kotlin.flappybird.actor.pipeDoorCycle
import jcornaz.demo.kotlin.flappybird.physics.GameWorld
import ktx.actors.plusAssign
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.graphics.use

private const val MAX_FRAME_DURATION = 1f / 25f

class MainScreen(private val assetBundle: AssetBundle) : EndableScreen(), KtxScreen {

  private val gameWorld = GameWorld()

  private val camera = OrthographicCamera().apply {
    position.y = WORLD_HEIGHT / 2f
  }

  private val stage = Stage(FillViewport(WORLD_WIDTH, WORLD_HEIGHT, camera))
  private val bird = Bird(gameWorld, assetBundle.birdTexture)

  private val batch = SpriteBatch()
  private val screenViewPort = ScreenViewport()

  private var score = 0

  init {
    stage += floorCycle(gameWorld, assetBundle.floorTexture)
    stage += pipeDoorCycle(gameWorld, assetBundle.pipeTexture) {
      if (bird.isAlive) ++score
    }
    stage += bird

    stage.keyboardFocus = bird
  }

  override fun show() {
    Gdx.input.inputProcessor = stage
  }

  override fun render(delta: Float) {
    val elapsedTime = minOf(delta, MAX_FRAME_DURATION)

    gameWorld.step(elapsedTime)

    camera.position.x = gameWorld.bird.worldCenter.x

    stage.act(elapsedTime)

    clearScreen(0.6f, 0.9f, 1f)
    stage.viewport.apply()
    stage.draw()

    screenViewPort.apply()
    batch.use {
      assetBundle.font.draw(it, "Score: $score", 10f, screenViewPort.screenHeight - 10f)
    }

    if (!bird.isAlive) notifyCompleted()
  }

  override fun resize(width: Int, height: Int) {
    stage.viewport.update(width, height)
    screenViewPort.update(width, height)
  }

  override fun dispose() {
    stage.actors.dispose()
    stage.dispose()
    batch.dispose()
    gameWorld.dispose()
  }
}

private fun Iterable<Actor>.dispose() {
  forEach {
    if (it is Group) it.children.dispose()
    if (it is Disposable) it.dispose()
  }
}
