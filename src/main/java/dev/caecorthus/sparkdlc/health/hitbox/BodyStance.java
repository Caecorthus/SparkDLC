package dev.caecorthus.sparkdlc.health.hitbox;

/**
 * How a player's body is laid out for its part hitboxes: which way the body axis runs and which variant of the part
 * layout applies ({@link BodyHitboxRules}). Pure data, shared by both sides.
 * 玩家身体在部位命中箱中的摆放方式：身体轴线的走向，以及采用哪种部位布局（{@link BodyHitboxRules}）。纯数据，两端共用。
 */
public enum BodyStance {
    /** Upright at full height. / 直立，全高。 */
    STANDING,
    /** Upright with the head lowered as the sneaking model draws it; legs keep their length. / 直立但头部按潜行模型下移；腿长不变。 */
    CROUCHING,
    /** Riding a seat: torso, arms and head upright, both legs stretched forward from the hips. / 坐在座位上：躯干、手臂与头部直立，双腿自髋部向前伸出。 */
    SEATED,
    /**
     * Face down along the look direction, drawn one block behind the position (swimming and crawling).
     * 面朝下、沿视线方向俯卧，模型绘制在位置后方一格处（游泳与爬行）。
     */
    PRONE,
    /** Face down along the look direction, soles at the position (elytra flight, riptide). / 面朝下、沿视线方向，脚底位于位置处（鞘翅飞行、激流）。 */
    GLIDING,
    /** Lying face up, head towards the bed's head (sleeping). / 仰卧，头朝床头（睡觉）。 */
    SUPINE,
    /**
     * Drawn as a four-legged animal with the player's head (the Pig trait): the front legs are the arms, the hind legs
     * the legs, the front half of the trunk the chest and the rear half the abdomen.
     * 被绘制为带玩家头颅的四足动物（猪词条）：前腿即手臂，后腿即腿，躯干前半为胸部、后半为腹部。
     */
    ALL_FOURS
}
