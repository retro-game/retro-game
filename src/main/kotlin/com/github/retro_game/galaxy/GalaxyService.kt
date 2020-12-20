package com.github.retro_game.galaxy

import com.github.retro_game.body.Body
import com.github.retro_game.body.Debris
import com.github.retro_game.player.Alliance
import com.github.retro_game.player.NoobProtectionRank
import com.github.retro_game.player.Player
import com.github.retro_game.retro_game.cache.AllianceTagCache
import com.github.retro_game.retro_game.cache.StatisticsCache
import com.github.retro_game.retro_game.cache.UserAllianceCache
import com.github.retro_game.retro_game.dto.ActiveStateDto.INACTIVE_LONG
import com.github.retro_game.retro_game.dto.ActiveStateDto.INACTIVE_SHORT
import com.github.retro_game.retro_game.dto.NoobProtectionRankDto
import com.github.retro_game.retro_game.entity.BodyType
import com.github.retro_game.retro_game.entity.GalaxySlot
import com.github.retro_game.retro_game.repository.GalaxySlotRepository
import com.github.retro_game.retro_game.security.CustomUser
import com.github.retro_game.retro_game.service.ActivityService
import com.github.retro_game.retro_game.service.impl.NoobProtectionService
import com.github.retro_game.retro_game.service.impl.UserServiceInternal
import org.springframework.stereotype.Service
import java.time.Instant
import com.github.retro_game.body.BodyType as PlanetType

@Service
class GalaxyService(
    private val galaxySlotRepository: GalaxySlotRepository,
    private val allianceTagCache: AllianceTagCache?,
    private val statisticsCache: StatisticsCache,
    private val userAllianceCache: UserAllianceCache,
    private val activityService: ActivityService,
    private val noobProtectionService: NoobProtectionService,
    private val userServiceInternal: UserServiceInternal
) {

    fun getSlots(galaxy: Int, system: Int): Map<Int, GalaxySlotDTO> {
        val userId = CustomUser.getCurrentUserId()

        return galaxySlotRepository
            .findAllByGalaxyAndSystem(galaxy, system)
            .map { slot ->
                slot.position to GalaxySlotDTO(
                    player = playerForSlot(slot, userId),
                    planet = planet(slot),
                    moon = moonOrNull(slot),
                    debris = slot.debris(),
                    own = slot.userId == userId
                )
            }.toMap()
    }

    private fun planet(slot: GalaxySlot) = Body(
        name = slot.planetName,
        type = slot.planetType.toBodyType(),
        image = slot.planetImage,
        activity = slot.planetId.calculateActivity()
    )

    private fun playerForSlot(slot: GalaxySlot, userId: Long): Player {
        val activityState = activityService.activeState(userId)
        return Player(
            userId = slot.userId,
            userName = slot.userName,
            rank = slot.userId.toRank(),
            onVacation = slot.vacationUntil != null,
            banned = userServiceInternal.isBanned(slot.vacationUntil, slot.isForcedVacation),
            alliance = slot.userId.playersAlliance(),
            noobProtectionRank = noobProtectionService.getOtherPlayerRank(userId, slot.userId).toNoobProtection(),
            inactiveShort = activityState == INACTIVE_SHORT,
            inactiveLong = activityState == INACTIVE_LONG
        )
    }

    private fun NoobProtectionRankDto.toNoobProtection(): NoobProtectionRank = when (this) {
        NoobProtectionRankDto.NOOB -> NoobProtectionRank.NOOB
        NoobProtectionRankDto.EQUAL -> NoobProtectionRank.EQUAL
        NoobProtectionRankDto.STRONG -> NoobProtectionRank.STRONGER
    }

    private fun Long.playersAlliance(): Alliance? {
        return userAllianceCache
            .getUserAlliance(this)
            ?.let { allianceId -> Alliance(allianceId, allianceTagCache?.getTag(allianceId) ?: "") }
    }

    private fun Long.toRank(): Int {
        return statisticsCache.getUserSummary(this)?.overall?.rank ?: 0
    }

    private fun GalaxySlot.debris() = Debris(debrisMetal ?: 0, debrisCrystal ?: 0)

    private fun Long.calculateActivity(): Int {
        val currentActivity = activityService.getBodyActivity(this)
        return currentActivity?.roundCurrentActivity()!!
    }

    private fun Long?.roundCurrentActivity(): Int {
        val now = (Instant.now().epochSecond - (this ?: 0)) / 60
        return when {
            now < 15 -> 0
            now >= 60 -> 60
            else -> now.toInt()
        }
    }

    private fun BodyType.toBodyType(): PlanetType = when (this) {
        BodyType.MOON -> PlanetType.MOON
        BodyType.DRY -> PlanetType.DRY
        BodyType.DESERT -> PlanetType.DESERT
        BodyType.JUNGLE -> PlanetType.JUNGLE
        BodyType.NORMAL -> PlanetType.NORMAL
        BodyType.WATER -> PlanetType.WATER
        BodyType.ICE -> PlanetType.ICE
        BodyType.GAS -> PlanetType.GAS
    }

    private fun moonOrNull(slot: GalaxySlot): Body? {
        return if (slot.moonId != null) {
            Body(
                name = slot.moonName,
                type = PlanetType.MOON,
                image = slot.moonImage,
                activity = slot.moonId.calculateActivity()
            )
        } else {
            null
        }
    }

    fun nextGalaxy(current: Int): Int = current % 5 + 1
    fun previousGalaxy(current: Int): Int {
        return if (current - 1 > 0) {
            current - 1
        } else {
            5
        }
    }

    fun nextSystem(current: Int): Int = current % 500 + 1
    fun previousSystem(current: Int): Int {
        return if (current - 1 > 0) {
            current - 1
        } else {
            500
        }
    }
}
