package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.entity.Alliance;
import com.github.retro_game.retro_game.entity.AllianceMember;
import com.github.retro_game.retro_game.entity.User;

class UserAndAllianceAndMemberTuple {
  final User user;
  final Alliance alliance;
  final AllianceMember member;

  UserAndAllianceAndMemberTuple(User user, Alliance alliance, AllianceMember member) {
    this.user = user;
    this.alliance = alliance;
    this.member = member;
  }
}
