package br.cefetmg.games;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;

public class CauldronGame extends ApplicationAdapter {

	private Camera camera;
	private ModelBatch modelBatch;
	private Environment ambiente;
	private AssetManager assets;

	private Map<String, ModelInstance> caldeirao, fogueira;
	private Map<String, ParticleEffect> fogo, bolhas;
	private Map<String, Node> sopa;
	private ParticleSystem sistemaParticulas;
	private CameraInputController cameraController;
	private Music musica;

	private boolean aindaEstaCarregando = false;
	private float anguloRotacaoSopa = 0;

	@Override
	public void create() {
		// define a cor da borracha (fundo)
		Gdx.gl20.glClearColor(1, 1, 1, 1);

		// instancia batch, asset manager e ambiente 3D
		modelBatch = new ModelBatch();
		assets = new AssetManager();
		ambiente = new Environment();
		ambiente.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		ambiente.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		// configura a câmera
		float razaoAspecto = ((float) Gdx.graphics.getWidth()) / Gdx.graphics.getHeight();
		camera = new PerspectiveCamera(67, 480f * razaoAspecto, 480f);
		camera.position.set(1f, 1.75f, 3f);
		camera.lookAt(0, 0.35f, 0);
		camera.near = 1f;
		camera.far = 300f;
		camera.update();
		cameraController = new CameraInputController(camera);
		Gdx.input.setInputProcessor(cameraController);

		// solicita carregamento dos 2 modelos 3D da cena
		assets.load("caldeirao.obj", Model.class);
		assets.load("caldeirao-jogos.obj", Model.class);
		assets.load("caldeirao-love.obj", Model.class);
		assets.load("fogueira.obj", Model.class);

		// instancia e configura 2 tipos de renderizadores de partículas:
		// 1. Billboards (para fogo)
		// 2. PointSprites (para bolhas)
		BillboardParticleBatch billboardBatch = new BillboardParticleBatch(ParticleShader.AlignMode.Screen, true, 500);
		PointSpriteParticleBatch pointSpriteBatch = new PointSpriteParticleBatch();
		sistemaParticulas = new ParticleSystem();
		billboardBatch.setCamera(camera);
		pointSpriteBatch.setCamera(camera);
		sistemaParticulas.add(billboardBatch);
		sistemaParticulas.add(pointSpriteBatch);

		// solicita o carregamento dos efeitos de partículas
		ParticleEffectLoader.ParticleEffectLoadParameter loadParam = new ParticleEffectLoader.ParticleEffectLoadParameter(
				sistemaParticulas.getBatches());
		assets.load("fogo.pfx", ParticleEffect.class, loadParam);
		assets.load("fogo-jogos.pfx", ParticleEffect.class, loadParam);
		assets.load("fogo-love.pfx", ParticleEffect.class, loadParam);
		assets.load("bolhas.pfx", ParticleEffect.class, loadParam);
		assets.load("bolhas-jogos.pfx", ParticleEffect.class, loadParam);
		assets.load("bolhas-love.pfx", ParticleEffect.class, loadParam);

		// solicita carregamento da música
		musica = Gdx.audio.newMusic(Gdx.files.internal("zelda-potion-shop.mp3"));

		aindaEstaCarregando = true;
	}

	private void aoTerminoDoCarregamento() {
		caldeirao = new HashMap<String, ModelInstance>();
		fogueira = new HashMap<String, ModelInstance>();
		sopa = new HashMap<String, Node>();
		fogo = new HashMap<String, ParticleEffect>();
		bolhas = new HashMap<String, ParticleEffect>();

		aoTerminoDoCarregamento(0, "");
		aoTerminoDoCarregamento(2.0f, "-jogos");
		aoTerminoDoCarregamento(-2.0f, "-love");

		// começa a música
		musica.setLooping(true);
		musica.play();

		aindaEstaCarregando = false;
	}

	private void aoTerminoDoCarregamento(float x_offset, String name) {
		// configura instâncias de caldeirão e fogueira
		ModelInstance thisCaldeirao = new ModelInstance(assets.get("caldeirao" + name + ".obj", Model.class));
		thisCaldeirao.transform.setToTranslation(x_offset, 0, 0);
		ModelInstance thisFogueira = new ModelInstance(assets.get("fogueira.obj", Model.class));
		thisFogueira.transform.setToTranslation(x_offset, -0.08f, 0);
		Node thisSopa = thisCaldeirao.getNode("topoDaSopa");

		// instancia, configura e dá início ao efeito de fogo
		ParticleEffect thisFogo = ((ParticleEffect) assets.get("fogo" + name + ".pfx")).copy();
		thisFogo.init();
		thisFogo.start();
		thisFogo.translate(new Vector3(x_offset, 0.1f, 0));
		sistemaParticulas.add(thisFogo);

		// instancia, configura e dá início ao efeito das bolhas
		// use o campo ParticleEffect bolhas definido na linha #38
		ParticleEffect thisBolhas = ((ParticleEffect) assets.get("bolhas" + name + ".pfx")).copy();
		thisBolhas.init();
		thisBolhas.start();
		thisBolhas.translate(new Vector3(x_offset, 1.1f, 0));
		sistemaParticulas.add(thisBolhas);

		caldeirao.put(name, thisCaldeirao);
		fogueira.put(name, thisFogueira);
		sopa.put(name, thisSopa);
		fogo.put(name, thisFogo);
		bolhas.put(name, thisBolhas);
	}

	@Override
	public void resize(int width, int height) {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		// configura a câmera para as novas dimensões da janela
		float razaoAspecto = (float) width / (float) height;
		camera.viewportWidth = 480f * razaoAspecto;
		camera.viewportHeight = 480f;
		camera.update();
	}

	@Override
	public void render() {
		// assim que tiver terminado de carregar os modelos e efeitos de
		// partículas, invoca a função para instanciar e configurar a cena
		// (aoTerminoDoCarregamento())
		if (aindaEstaCarregando && assets.update()) {
			aoTerminoDoCarregamento();
		}

		// atualiza a câmera de acordo com as configurações do mouse
		cameraController.update();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// começa a desenhar os modelos da cena
		modelBatch.begin(camera);

		for (String name : new String[] { "", "-love", "-jogos" }) {
			// se já tiver carregado o caldeirão, renderiza-o
			if (caldeirao != null && caldeirao.containsKey(name)) {
				Node thisSopa = sopa.get(name);
				thisSopa.scale.set(0.99f, 1, 0.99f);
				thisSopa.rotation.set(Vector3.Y, anguloRotacaoSopa++);
				thisSopa.calculateTransforms(true);
				modelBatch.render(caldeirao.get(name), ambiente);
			}

			// se já tiver carregado o fogueira, renderiza-a
			if (fogueira != null && fogueira.containsKey(name)) {
				modelBatch.render(fogueira.get(name), ambiente);
			}

		}

		// se já tiver terminado todo o carregamento, renderiza os efeitos
		// de partículas
		if (!aindaEstaCarregando) {
			sistemaParticulas.update();
			sistemaParticulas.begin();
			sistemaParticulas.draw();
			sistemaParticulas.end();
			modelBatch.render(sistemaParticulas);
		}
		modelBatch.end();
	}

	@Override
	public void dispose() {
		// desaloca os recursos da cena
		modelBatch.dispose();
		assets.dispose();
		for (ParticleEffect pe : fogo.values()) {
			pe.dispose();
		}
		for (ParticleEffect pe : bolhas.values()) {
			pe.dispose();
		}
		musica.dispose();
	}
}
