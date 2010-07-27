package org.rsbot.event.impl;

import java.awt.Graphics;

import org.rsbot.accessors.Client;
import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.util.StringUtil;

public class TCamera implements TextPaintListener {
	
	private Client client;
	
    public TCamera(Bot bot) {
    	client = bot.getClient();
    }

    public int drawLine(final Graphics render, int idx) {
        final String camPos = "Camera Position (x,y,z): (" + client.getCamPosX() + ", " + client.getCamPosY() + ", " + client.getCamPosZ() + ")";
        final String camAngle = "Camera Angle (pitch, yaw): (" + client.getCameraPitch() + ", " + client.getCameraYaw() + ")";
        //final String detailLvl = "Detail lvl: " + (client.getDetailInfo() != null ? client.getDetailInfo().getDetailLevel() : "null");

        StringUtil.drawLine(render, idx++, camPos);
        StringUtil.drawLine(render, idx++, camAngle);
        //Methods.drawLine(render, idx++, detailLvl);
        return idx;
    }
}
