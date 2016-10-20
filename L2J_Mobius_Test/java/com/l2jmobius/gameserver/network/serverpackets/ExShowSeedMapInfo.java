/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jmobius.gameserver.network.serverpackets;

import com.l2jmobius.gameserver.instancemanager.GraciaSeedsManager;

public class ExShowSeedMapInfo extends L2GameServerPacket
{
	public static final ExShowSeedMapInfo STATIC_PACKET = new ExShowSeedMapInfo();
	
	private ExShowSeedMapInfo()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE); // Id
		writeH(0xA2); // SubId
		
		writeD(2); // seed count
		
		// Seed of Destruction
		writeD(-246857); // x coord
		writeD(251960); // y coord
		writeD(4331); // z coord
		writeD(2770 + GraciaSeedsManager.getInstance().getSoDState()); // sys msg id
		
		// Seed of Infinity
		writeD(-178472); // x coord
		writeD(152538); // y coord
		writeD(2544); // z coord
		// Manager not implemented yet
		writeD(3302); // sys msg id
	}
}
