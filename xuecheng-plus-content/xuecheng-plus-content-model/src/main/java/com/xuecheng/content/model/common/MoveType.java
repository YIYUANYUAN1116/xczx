package com.xuecheng.content.model.common;

public enum MoveType {
    MOVE_DOWN("movedown"),
    MOVE_UP("moveup");

    private String moveType;

    MoveType(String moveType) {
        this.moveType = moveType;
    }

    public void setMoveType(String moveType) {
        this.moveType = moveType;
    }

    public String getMoveType( ) {
        return moveType;
    }
}
