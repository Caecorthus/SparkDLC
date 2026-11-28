package dev.caecorthus.sparkdlc.client.render.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.CubeFace;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Renders a Blockbench model JSON that uses per-element multi-axis rotations, which vanilla's model loader rejects
 * (it only accepts one axis at a time and ±45°). The JSON is drawn quad by quad with the same corner order, UV rect
 * and sprite lookup vanilla's {@code BakedQuadFactory} uses, so the result matches what a plain model would look like.
 * Used by the placed reality anchor; its item icon is a normal 2D model.
 * 渲染带逐元素多轴旋转的 Blockbench 模型 JSON——原版模型加载器只接受单轴且 ±45° 以内的旋转，会整份拒绝。这里按原版
 * {@code BakedQuadFactory} 相同的角点顺序、UV 矩形与图集取样逐面绘制，效果等同普通模型。用于放置后的现实稳定锚；
 * 它的物品图标是普通 2D 模型。
 *
 * <p>The file lives outside {@code models/} on purpose: vanilla parses every JSON under {@code models/} at each
 * resource load and would log this one as a broken model. It is parsed and baked into quads once per resource reload
 * ({@link #reload}); a frame only looks up the sprite, which a reload re-stitches, and emits the baked vertices.
 * <p>该文件有意放在 {@code models/} 之外：原版每次加载资源都会解析 {@code models/} 下的所有 JSON，会把这一份当作损坏的模型
 * 记入日志。它在每次资源重载时解析并烘焙成面片一次（{@link #reload}）；每帧只查询贴图（重载会重新拼接图集）并输出烘焙好的
 * 顶点。
 */
public final class BlockbenchRotatedModel {
    private static final Identifier MODEL_ID = SparkDLC.id("blockbench/reality_anchor.json");
    private static final Identifier TEXTURE_ID = SparkDLC.id("item/reality_anchor");

    /** One cuboid with its rotation. / 一个带旋转的立方体。 */
    private record Element(float[] min, float[] max, float[] rotationOrigin, float rotationX, float rotationY,
                           float rotationZ, Map<Direction, float[]> faces, String name) {
    }

    /**
     * One baked face: four corners (x, y, z in blocks), their (u, v) as fractions of the sprite, and the normal.
     * 一个烘焙好的面：四个角点（以方块为单位的 x、y、z）、各自在贴图内的 (u, v) 比例，以及法线。
     */
    private record Quad(float[] positions, float[] uvs, float normalX, float normalY, float normalZ) {
    }

    private static List<Quad> quads = List.of();

    private BlockbenchRotatedModel() {
    }

    /**
     * Re-reads and re-bakes the model; called for every client resource reload, the first one included. A missing or
     * broken file is logged once per reload and leaves nothing to draw.
     * 重新读取并烘焙模型；每次客户端资源重载（包括第一次）都会调用。文件缺失或损坏时每次重载记录一条日志，并且不绘制任何东西。
     */
    public static void reload(ResourceManager manager) {
        quads = load(manager);
    }

    public static void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        List<Quad> baked = quads;
        if (baked.isEmpty()) {
            return;
        }
        Sprite sprite = MinecraftClient.getInstance().getBakedModelManager()
                .getAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).getSprite(TEXTURE_ID);
        VertexConsumer consumer = vertexConsumers.getBuffer(TexturedRenderLayers.getItemEntityTranslucentCull());
        MatrixStack.Entry entry = matrices.peek();
        for (Quad quad : baked) {
            float[] positions = quad.positions();
            float[] uvs = quad.uvs();
            for (int corner = 0; corner < 4; corner++) {
                consumer.vertex(entry, positions[corner * 3], positions[corner * 3 + 1], positions[corner * 3 + 2])
                        .color(1.0F, 1.0F, 1.0F, 1.0F)
                        .texture(sprite.getFrameU(uvs[corner * 2]), sprite.getFrameV(uvs[corner * 2 + 1]))
                        .overlay(overlay)
                        .light(light)
                        .normal(entry, quad.normalX(), quad.normalY(), quad.normalZ());
            }
        }
    }

    private static Quad bakeFace(Element element, Direction direction, float[] uvs) {
        CubeFace cubeFace = CubeFace.getFace(direction);
        float[] positions = positionMatrix(element);
        float[] corners = new float[12];
        for (int corner = 0; corner < 4; corner++) {
            CubeFace.Corner c = cubeFace.getCorner(corner);
            float x = positions[c.xSide];
            float y = positions[c.ySide];
            float z = positions[c.zSide];
            double[] rotated = rotate(element, x, y, z);
            corners[corner * 3] = (float) rotated[0];
            corners[corner * 3 + 1] = (float) rotated[1];
            corners[corner * 3 + 2] = (float) rotated[2];
        }
        double[] normal = rotateDirection(element, direction.getOffsetX(), direction.getOffsetY(),
                direction.getOffsetZ());
        // Same UV rect mapping as ModelElementTexture.getU/getV. Elements rotated off the 180° grid (the hex
        // prisms' slanted sides) draw their textures upside down, so only the V corners are swapped; the U
        // corners stay as they are, which keeps the texture un-mirrored left to right.
        // 与 ModelElementTexture 相同的 UV 取角。旋转不在 180° 整数倍上的元素（六棱柱斜面）贴图上下颠倒，因此只对调 V 角，
        // U 角保持原样，左右不会镜像。
        boolean flip = flipsUv(element) && !keepsPlainUv(element);
        float[] cornerUvs = new float[8];
        for (int corner = 0; corner < 4; corner++) {
            float u = (corner == 0 || corner == 1) ? uvs[0] : uvs[2];
            float v = (corner == 0 || corner == 3)
                    ? (flip ? uvs[3] : uvs[1])
                    : (flip ? uvs[1] : uvs[3]);
            cornerUvs[corner * 2] = u / 16.0F;
            cornerUvs[corner * 2 + 1] = v / 16.0F;
        }
        return new Quad(corners, cornerUvs, (float) normal[0], (float) normal[1], (float) normal[2]);
    }

    /**
     * The two end plates ({@code face_301} and the matching {@code face_60}) were exported with mirrored local axes
     * already, so their UVs must NOT get the 180° V turn the other slanted elements need.
     * 两块端板（{@code face_301} 与对应的 {@code face_60}）导出时已带镜像的局部轴，因此不需要其他斜面那样的 V 轴翻转。
     */
    private static boolean keepsPlainUv(Element element) {
        if ("face_301".equals(element.name())) {
            return true;
        }
        float sizeX = Math.abs(element.max()[0] - element.min()[0]);
        float sizeY = Math.abs(element.max()[1] - element.min()[1]);
        float sizeZ = Math.abs(element.max()[2] - element.min()[2]);
        return "face_60".equals(element.name())
                && Math.abs(sizeX - 2.0F) < 0.01F
                && Math.abs(sizeY - 0.9282F) < 0.01F
                && Math.abs(sizeZ - 8.0F) < 0.01F;
    }

    /** Whether the element sits off the 180° rotation grid, whose faces need the 180° texture turn. / 元素是否需要 180° 贴图翻转。 */
    private static boolean flipsUv(Element element) {
        return Math.abs(element.rotationX() % 180.0F) > 0.01F
                || Math.abs(element.rotationY() % 180.0F) > 0.01F
                || Math.abs(element.rotationZ() % 180.0F) > 0.01F;
    }

    /** Model-unit positions per face axis, matching BakedQuadFactory.getPositionMatrix. / 面轴上的模型坐标。 */
    private static float[] positionMatrix(Element element) {
        float[] positions = new float[6];
        positions[Direction.DOWN.ordinal()] = element.min()[1] / 16.0F;
        positions[Direction.UP.ordinal()] = element.max()[1] / 16.0F;
        positions[Direction.NORTH.ordinal()] = element.min()[2] / 16.0F;
        positions[Direction.SOUTH.ordinal()] = element.max()[2] / 16.0F;
        positions[Direction.WEST.ordinal()] = element.min()[0] / 16.0F;
        positions[Direction.EAST.ordinal()] = element.max()[0] / 16.0F;
        return positions;
    }

    /** Rotates a model-unit point around the element's origin by its X, then Y, then Z rotation. / 绕元素原点旋转。 */
    private static double[] rotate(Element element, float x, float y, float z) {
        double px = x - element.rotationOrigin()[0] / 16.0F;
        double py = y - element.rotationOrigin()[1] / 16.0F;
        double pz = z - element.rotationOrigin()[2] / 16.0F;
        double[] r = rotateDirection(element, px, py, pz);
        return new double[]{r[0] + element.rotationOrigin()[0] / 16.0F,
                r[1] + element.rotationOrigin()[1] / 16.0F,
                r[2] + element.rotationOrigin()[2] / 16.0F};
    }

    private static double[] rotateDirection(Element element, double x, double y, double z) {
        double ax = Math.toRadians(element.rotationX());
        double ay = Math.toRadians(element.rotationY());
        double az = Math.toRadians(element.rotationZ());
        double cosX = Math.cos(ax);
        double sinX = Math.sin(ax);
        double y1 = y * cosX - z * sinX;
        double z1 = y * sinX + z * cosX;
        double cosY = Math.cos(ay);
        double sinY = Math.sin(ay);
        double x2 = x * cosY + z1 * sinY;
        double z2 = -x * sinY + z1 * cosY;
        double cosZ = Math.cos(az);
        double sinZ = Math.sin(az);
        double x3 = x2 * cosZ - y1 * sinZ;
        double y3 = x2 * sinZ + y1 * cosZ;
        return new double[]{x3, y3, z2};
    }

    private static List<Quad> load(ResourceManager manager) {
        try (InputStreamReader reader = new InputStreamReader(
                manager.getResourceOrThrow(MODEL_ID).getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray array = json.getAsJsonArray("elements");
            List<Quad> baked = new ArrayList<>();
            for (JsonElement raw : array) {
                Element element = parseElement(raw.getAsJsonObject());
                for (Map.Entry<Direction, float[]> face : element.faces().entrySet()) {
                    baked.add(bakeFace(element, face.getKey(), face.getValue()));
                }
            }
            return List.copyOf(baked);
        } catch (Exception exception) {
            SparkDLC.LOGGER.warn("Could not load the reality anchor model {}", MODEL_ID, exception);
            return List.of();
        }
    }

    private static Element parseElement(JsonObject object) {
        JsonArray from = object.getAsJsonArray("from");
        JsonArray to = object.getAsJsonArray("to");
        float rotationX = 0.0F;
        float rotationY = 0.0F;
        float rotationZ = 0.0F;
        float[] origin = {0.0F, 0.0F, 0.0F};
        if (object.has("rotation")) {
            JsonObject rotation = object.getAsJsonObject("rotation");
            rotationX = rotation.get("x").getAsFloat();
            rotationY = rotation.get("y").getAsFloat();
            rotationZ = rotation.get("z").getAsFloat();
            JsonArray originArray = rotation.getAsJsonArray("origin");
            origin = new float[]{originArray.get(0).getAsFloat(), originArray.get(1).getAsFloat(),
                    originArray.get(2).getAsFloat()};
        }
        Map<Direction, float[]> faces = new EnumMap<>(Direction.class);
        JsonObject faceObject = object.getAsJsonObject("faces");
        for (Map.Entry<String, JsonElement> face : faceObject.entrySet()) {
            Direction direction = directionOf(face.getKey());
            if (direction == null) {
                continue;
            }
            JsonArray uv = face.getValue().getAsJsonObject().getAsJsonArray("uv");
            faces.put(direction, new float[]{uv.get(0).getAsFloat(), uv.get(1).getAsFloat(),
                    uv.get(2).getAsFloat(), uv.get(3).getAsFloat()});
        }
        return new Element(
                new float[]{from.get(0).getAsFloat(), from.get(1).getAsFloat(), from.get(2).getAsFloat()},
                new float[]{to.get(0).getAsFloat(), to.get(1).getAsFloat(), to.get(2).getAsFloat()},
                origin, rotationX, rotationY, rotationZ, faces,
                object.has("name") ? object.get("name").getAsString() : "");
    }

    private static Direction directionOf(String name) {
        return switch (name) {
            case "north" -> Direction.NORTH;
            case "south" -> Direction.SOUTH;
            case "east" -> Direction.EAST;
            case "west" -> Direction.WEST;
            case "up" -> Direction.UP;
            case "down" -> Direction.DOWN;
            default -> null;
        };
    }
}
