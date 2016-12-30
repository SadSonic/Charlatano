/*
 * Charlatano is a premium CS:GO cheat ran on the JVM.
 * Copyright (C) 2016 Thomas Nappo, Jonathan Beaudoin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.charlatano.game.entity

import com.charlatano.AIM_BONE
import com.charlatano.game.CSGO.ENTITY_SIZE
import com.charlatano.game.CSGO.clientDLL
import com.charlatano.game.CSGO.csgoEXE
import com.charlatano.game.netvars.NetVarOffsets.bIsScoped
import com.charlatano.game.netvars.NetVarOffsets.dwBoneMatrix
import com.charlatano.game.netvars.NetVarOffsets.fFlags
import com.charlatano.game.netvars.NetVarOffsets.hActiveWeapon
import com.charlatano.game.netvars.NetVarOffsets.iHealth
import com.charlatano.game.netvars.NetVarOffsets.iWeaponID
import com.charlatano.game.netvars.NetVarOffsets.lifeState
import com.charlatano.game.netvars.NetVarOffsets.nTickBase
import com.charlatano.game.netvars.NetVarOffsets.vecPunch
import com.charlatano.game.netvars.NetVarOffsets.vecVelocity
import com.charlatano.game.netvars.NetVarOffsets.vecViewOffset
import com.charlatano.game.offsets.ClientOffsets.dwEntityList
import com.charlatano.utils.Angle
import com.charlatano.utils.Vector
import com.charlatano.utils.Weapons
import com.charlatano.utils.extensions.uint
import org.jire.arrowhead.get

typealias Player = Long

internal fun Player.weapon(): Weapons {
	val address: Int = csgoEXE[this + hActiveWeapon]
	val index = address and 0xFFF
	val base: Int = clientDLL[dwEntityList + (index - 1) * ENTITY_SIZE]
	
	var id = 42
	if (base > 0)
		id = csgoEXE[base + iWeaponID]
	
	return Weapons[id]
}

internal fun Player.flags(): Int = csgoEXE[this + fFlags]

internal fun Player.onGround() = flags() and 1 == 1

internal fun Player.health(): Int = csgoEXE[this + iHealth]

internal fun Player.dead() = try {
	(csgoEXE.byte(this + lifeState) != 0.toByte()) || health() <= 0
} catch (t: Throwable) {
	false
}

internal fun Player.punch(): Angle
		= Vector(csgoEXE.float(this + vecPunch).toDouble(), csgoEXE.float(this + vecPunch + 4).toDouble(), 0.0)

internal fun Player.viewOffset(): Angle
		= Vector(csgoEXE.float(this + vecViewOffset).toDouble(),
		csgoEXE.float(this + vecViewOffset + 4).toDouble(),
		csgoEXE.float(this + vecViewOffset + 8).toDouble())

internal fun Player.velocity(): Angle
		= Vector(csgoEXE.float(this + vecVelocity).toDouble(),
		csgoEXE.float(this + vecVelocity + 4).toDouble(),
		csgoEXE.float(this + vecVelocity + 8).toDouble())

internal fun Player.boneMatrix() = csgoEXE.uint(this + dwBoneMatrix)

internal fun Player.bone(offset: Int, boneID: Int = AIM_BONE, boneMatrix: Long = boneMatrix())
		= csgoEXE.float(boneMatrix + ((0x30 * boneID) + offset)).toDouble()

internal fun Player.isScoped(): Boolean = csgoEXE[this + bIsScoped]

const val TICK_RATIO = 1F / 64F

internal fun Player.time(): Float = csgoEXE.int(this + nTickBase) * TICK_RATIO