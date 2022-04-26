#
<<<<<<< HEAD:make/common/modules/GensrcCommon.gmk
# Copyright (c) 2011, 2022, Oracle and/or its affiliates. All rights reserved.
=======
# Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
>>>>>>> master:make/data/autoheaders/assemblyprefix.h
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#

<<<<<<< HEAD:make/common/modules/GensrcCommon.gmk
include Execute.gmk
include TextFileProcessing.gmk
include ToolsJdk.gmk

################################################################################
# Sets up a rule that creates a version.properties file in the gensrc output
# directory.
# Param 1 - Variable to add generated file name to
# Param 2 - Name of version.properties file including packages from the src
#           root.
define SetupVersionProperties
  $(SUPPORT_OUTPUTDIR)/gensrc/$(MODULE)/$$(strip $2):
	$$(call MakeTargetDir)
	$(PRINTF) "jdk=$(VERSION_NUMBER)\nfull=$(VERSION_STRING)\nrelease=$(VERSION_SHORT)\n" \
	    > $$@

  $$(strip $1) += $(SUPPORT_OUTPUTDIR)/gensrc/$(MODULE)/$$(strip $2)
endef
=======
// ASSEMBLY_SRC_FILE gets replaced by relative or absolute file path
// in NativeCompilation.gmk for gcc tooling on Linux. This ensures a
// reproducible object file through a predictable value of the STT_FILE
// symbol, and subsequently a reproducible .debuginfo.
.file ASSEMBLY_SRC_FILE

>>>>>>> master:make/data/autoheaders/assemblyprefix.h
