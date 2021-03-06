package com.nameless.game.actors.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.nameless.game.*;
import com.nameless.game.actors.Character;
import com.nameless.game.actors.Loot;
import com.nameless.game.actors.items.Flashlight;
import com.nameless.game.actors.items.Weapons;
import com.nameless.game.scene2d.ui.HealthBar;
import com.nameless.game.scene2d.ui.Info;
import com.nameless.game.scene2d.ui.WeaponsLabel;
import com.nameless.game.screens.BasicPlay;

import static com.nameless.game.Constants.ENEMY_OBSTACLES_BIT;
import static com.nameless.game.Constants.PixelsPerMeter;


public class Player extends Character{
    public BasicPlay play;

    public TextureAtlas atlas;
    public TextureRegion region;

    private Flashlight flashlight = null;

    public Vector2 MuzzlePos;

    public Weapons weapons;

    public Player(BasicPlay play, float x, float y) {
        super(300, 999999999);
        this.play = play;

        weapons = new Weapons();
        weapons.addWeapon(WeaponsInfo.NONE, 1);
        weapons.addWeapon(WeaponsInfo.PISTOL, WeaponsInfo.PISTOL_CAPACITY);
        weapons.addWeapon(WeaponsInfo.UZI, WeaponsInfo.UZI_CAPACITY);
        weapons.addWeapon(WeaponsInfo.SHOTGUN, WeaponsInfo.SHOTGUN_CAPACITY);
        weapons.addWeapon(WeaponsInfo.GRENADE, WeaponsInfo.GRENADE_CAPACITY);
        weapons.addWeapon(WeaponsInfo.ROCKET, WeaponsInfo.ROCKET_CAPACITY);

        atlas = MainGame.manager.get("players/sprites.atlas");
        switch (VirtualController.ACTUAL_WEAPON){
            case WeaponsInfo.ROCKET:
                region = atlas.findRegion(Constants.character + "_rocket");
                break;
            case WeaponsInfo.GRENADE:
                region = atlas.findRegion(Constants.character + "_grenade");
                break;
            case WeaponsInfo.SHOTGUN:
                region = atlas.findRegion(Constants.character + "_shotgun");
                break;
            case WeaponsInfo.UZI:
                region = atlas.findRegion(Constants.character + "_machine");
                break;
            case WeaponsInfo.PISTOL:
                region = atlas.findRegion(Constants.character + "_gun");
                break;
            case WeaponsInfo.NONE:
            default:
                region = atlas.findRegion(Constants.character + "_stand");
        }
        setPosition(x/PixelsPerMeter, y/PixelsPerMeter);
        setSize(atlas.findRegion(Constants.character + "_stand").getRegionWidth()/PixelsPerMeter,
                atlas.findRegion(Constants.character + "_stand").getRegionWidth()/PixelsPerMeter);
        setOrigin(getWidth()/2,getHeight()/1.6f);
        MuzzlePos = new Vector2(getX() + getWidth()/2, getY() + getHeight()/2)
                .add(getWidth()*1.5f, -getHeight()/6);


        setBox2d();

        if(rayHandler != null) flashlight = new Flashlight(rayHandler, getRotation(), getHeight(), body);

        currentState = new DefaultPlayerState();
        currentState.Enter(this);

        HealthBar healthbar = new HealthBar(this);
        WeaponsLabel weaponLabel = new WeaponsLabel(this);
        play.mapHud.addActor(healthbar);
        play.mapHud.addActor(weaponLabel);
    }

    private void setBox2d() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(getX(), getY());
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bdef);
        body.setUserData(this);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius((getWidth()/2));

        fdef.shape = shape;
        fdef.filter.categoryBits = Constants.PLAYER_BIT;
        fdef.filter.maskBits = Constants.everyOthersBit(ENEMY_OBSTACLES_BIT);
        body.createFixture(fdef);
        shape.dispose();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if(blinker.shouldBlink(Gdx.graphics.getDeltaTime())) return;

        batch.setColor(batch.getColor().r, batch.getColor().g, batch.getColor().b, getColor().a);
        batch.setColor(getColor());
        batch.draw(region, getX(), getY(), getOriginX(),getOriginY(), region.getRegionWidth()/PixelsPerMeter,
                region.getRegionHeight()/PixelsPerMeter, getScaleX(), getScaleY(), getRotation());
        batch.setColor(Color.WHITE);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        flashlight.update(DayNightCycleManager.lightsOpen);
        MuzzlePos.set(getCenterX(), getCenterY())
                .add(getWidth()*1f, -getHeight()/6);

        switch (VirtualController.ACTUAL_WEAPON){
            case WeaponsInfo.ROCKET:
                region = atlas.findRegion(Constants.character + "_rocket");
                break;
            case WeaponsInfo.GRENADE:
                region = atlas.findRegion(Constants.character + "_grenade");
                break;
            case WeaponsInfo.SHOTGUN:
                region = atlas.findRegion(Constants.character + "_shotgun");
                break;
            case WeaponsInfo.UZI:
                region = atlas.findRegion(Constants.character + "_machine");
                break;
            case WeaponsInfo.PISTOL:
                region = atlas.findRegion(Constants.character + "_gun");
                break;
            case WeaponsInfo.NONE:
            default:
                region = atlas.findRegion(Constants.character + "_stand");
        }
    }

    public boolean remove(){
        play.clearScene();
        world.destroyBody(body);
        currentState.Exit();
        currentState = null;
        return super.remove();
    }

    public void LootCollected(Loot.Type type, float x, float y){
        Info info = new Info(this, x, y, "None");
        switch (type){
            case WEAPON:
                info.setText("+ PISTOL");
                //label.setColor(Color.valueOf("#006e82"));
                break;
            case LIFE:
                HEALTH = MAX_HEALTH;
                info.setText("+ LIFE");
                //label.setColor(Color.CORAL);
                break;
            case AMMO:
                weapons.fillAmmo(VirtualController.ACTUAL_WEAPON, 500);
                info.setText("+ AMMO");
                //label.setColor(Color.OLIVE);
                break;
            default:
        }
        play.mapHud.addActor(info);
    }
}
