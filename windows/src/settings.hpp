#pragma once

#include "generator.hpp"

namespace pg {

Preferences LoadPreferences();
void SavePreferences(const Preferences& preferences);

}  // namespace pg
