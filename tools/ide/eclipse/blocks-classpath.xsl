<?xml version="1.0"?>

<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!-- @version $Id$ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <project default="generate-customized-eclipse-project" basedir="." name="blocks-eclipse">
            <description>Autogenerated Ant build file that builds customized eclipse project for selected blocks.</description>
            <!-- copied over from blocks-build.xsl -->
            <macrodef name="test-include-block">
                <attribute name="name"/>
                <sequential>
                    <condition property="include.block.@{{name}}">
                        <and>
                            <equals arg1="${{default.block.mode}}" arg2="include"/>
                            <not><istrue value="${{exclude.block.@{{name}}}}"/></not>
                        </and>
                    </condition>
                    <condition property="internal.exclude.block.@{{name}}">
                        <and>
                            <not><istrue value="${{unconditional.include.all.blocks}}"/></not>
                            <not><istrue value="${{include.block.@{{name}}}}"/></not>
                        </and>
                    </condition>
                </sequential>
            </macrodef>

            <macrodef name="eclipseclasspath-block">
                <attribute name="name"/>
                <sequential>
                    <!-- block src and test directory -->
                    <if>
                        <available file="${{blocks}}/@{{name}}/java" type="dir"/>
                        <then>
                            <path id="src-@{{name}}">
                                <dirset dir="${{blocks}}/@{{name}}">
                                    <include name="java"/>
                                    <include name="test"/>
                                </dirset>
                            </path>
                            <property name="src-@{{name}}" refid="src-@{{name}}"/>
                            <replace file="${{build.temp}}/classpath-temp.xml"
                                    token="@eclipse-src@" value="${{src-@{{name}}}}${{path.separator}}@eclipse-src@"/>
                        </then>
                    </if>
                    <!-- block mocks directory -->
                    <if>
                        <available file="${{blocks}}/@{{name}}/mocks" type="dir"/>
                        <then>
                            <path id="mocks-@{{name}}">
                                <dirset dir="${{blocks}}/@{{name}}">
                                    <include name="**/mocks"/>
                                </dirset>
                            </path>
                            <property name="mocks-@{{name}}" refid="mocks-@{{name}}"/>
                            <replace file="${{build.temp}}/classpath-temp.xml"
                                    token="@eclipse-mocks@" value="${{mocks-@{{name}}}}${{path.separator}}@eclipse-mocks@"/>
                        </then>
                    </if>
                    <!-- block lib directory (deprecated) -->
                    <if>
                        <available file="${{blocks}}/@{{name}}/lib" type="dir"/>
                        <then>
                            <path id="lib-@{{name}}">
                                <fileset dir="${{blocks}}/@{{name}}">
                                    <include name="**/*.jar"/>
                                </fileset>
                            </path>
                            <property name="lib-@{{name}}" refid="lib-@{{name}}"/>
                            <replace file="${{build.temp}}/classpath-temp.xml"
                                    token="@eclipse-libs@" value="${{lib-@{{name}}}}${{path.separator}}@eclipse-libs@"/>
                        </then>
                    </if>
                </sequential>
            </macrodef>

            <macrodef name="include-lib-block">
                <attribute name="name"/>
                <attribute name="lib-name"/>
                <sequential>
                    <if>
                        <not>
                            <istrue value="${{eclipse-optional-lib-@{{lib-name}}}}"/>
                        </not>
                        <then>
                            <property name="eclipse-optional-lib-@{{lib-name}}" value="true"/>
                            <path id="eclipse-optional-lib-@{{name}}-@{{lib-name}}">
                                <fileset dir="${{lib.optional}}">
                                    <include name="@{{lib-name}}*.jar"/>
                                </fileset>
                            </path>
                            <property name="eclipse-optional-lib-@{{name}}-@{{lib-name}}" refid="eclipse-optional-lib-@{{name}}-@{{lib-name}}"/>
                            <!-- The new lib cannot be empty -->
                            <if>
                                <not>
                                    <equals arg1="${{eclipse-optional-lib-@{{name}}-@{{lib-name}}}}" arg2=""/>
                                </not>
                                <then>
                                    <replace file="${{build.temp}}/classpath-temp.xml"
                                            token="@eclipse-libs@" value="${{eclipse-optional-lib-@{{name}}-@{{lib-name}}}}${{path.separator}}@eclipse-libs@"/>
                                </then>
                            </if>
                        </then>
                    </if>
                </sequential>
            </macrodef>

            <xsl:apply-templates select="module"/>
        </project>
    </xsl:template>

    <xsl:template match="module">
        <xsl:variable name="cocoon-blocks" select="project[starts-with(@name, 'cocoon-block-')]"/>

        <!-- Define wich blocks will be included -->
        <target name="init">
        	<!-- copied over from blocks-build.xsl -->
            <condition property="default.block.mode" value="include">
                <istrue value="${{include.all.blocks}}"/>
            </condition>
            <condition property="default.block.mode" value="exclude">
                <istrue value="${{exclude.all.blocks}}"/>
            </condition>
            <property name="default.block.mode" value="include"/>
            <xsl:for-each select="$cocoon-blocks">
                <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')"/>
                <test-include-block name="{$block-name}"/>
            </xsl:for-each>

            <!-- prepare the various paths that will form the project -->
            <path id="srcs">
                <!-- main source dir -->
                <pathelement path="${{src}}/java"/>
                <!-- samples source dir -->
                <!-- FIXME: Load based on local.build.properties -->
                <pathelement path="${{src}}/samples"/>
                <!-- deprecated source dir -->
                <!-- FIXME: Load based on local.build.properties -->
                <pathelement path="${{src}}/deprecated/java"/>
                <!-- test source dir -->
                <pathelement path="${{src}}/test"/>
            </path>
            <!--core mocks -->
            <path id="mockss">
                <dirset dir="${{src}}">
                    <include name="mocks"/>
                </dirset>
            </path>

            <path id="libs">
                <!-- main libs -->
                <fileset dir="${{lib}}">
                    <include name="core/*.jar"/>
                    <include name="local/*.jar"/>
                    <include name="endorsed/*.jar"/>
                </fileset>
                <!-- tools libs -->
                <fileset dir="${{tools}}/lib">
                    <include name="*.jar"/>
                </fileset>
            </path>

            <!-- convert paths to properties -->
            <property name="srcs" refid="srcs"/>
            <property name="mockss" refid="mockss"/>
            <property name="libs" refid="libs"/>

            <!-- start to expand properties in the template file -->
            <copy file="${{tools}}/ide/eclipse/classpath-tmpl.xml"
                    tofile="${{build.temp}}/classpath-temp.xml"
                    filtering="yes"
                    overwrite="yes">
                <filterset>
                    <filter token="SRC_DIRS" value="${{srcs}}${{path.separator}}@eclipse-src@"/>
                    <filter token="LIBS" value="${{libs}}${{path.separator}}@eclipse-libs@"/>
                    <filter token="MOCKS_DIRS" value="${{mockss}}${{path.separator}}@eclipse-mocks@"/>
                    <filter token="OUTPUT_DIR" value="${{ide.eclipse.outputdir}}"/>
                </filterset>
            </copy>
        </target>

        <target name="generate-customized-eclipse-project">
            <xsl:attribute name="depends">
                <xsl:text>init</xsl:text>
                <xsl:for-each select="$cocoon-blocks">
                    <xsl:text>,</xsl:text>
                    <xsl:value-of select="concat(@name, '-eclipseclasspath')"/>
                </xsl:for-each>
            </xsl:attribute>

            <!-- clean up src, libs and mocks  -->
            <replace file="${{build.temp}}/classpath-temp.xml"
                    token="${{path.separator}}@eclipse-src@" value=""/>

            <replace file="${{build.temp}}/classpath-temp.xml"
                    token="${{path.separator}}@eclipse-libs@" value=""/>
            <replace file="${{build.temp}}/classpath-temp.xml"
                    token="@eclipse-libs@" value=""/>
            <replace file="${{build.temp}}/classpath-temp.xml"
                    token="${{path.separator}}@eclipse-mocks@" value=""/>

            <!-- split the path in 'item' XML elements -->
            <replace file="${{build.temp}}/classpath-temp.xml"
                    token="${{path.separator}}" value="&lt;/item&gt;&#xA; &lt;item&gt;"/>
            <!-- relativize file names by removing the current directory -->
            <replace file="${{build.temp}}/classpath-temp.xml"
                    token="${{user}}${{file.separator}}" value=""/>
            <!-- and in case that fails, remove the base directory -->
            <replace file="${{build.temp}}/classpath-temp.xml"
                    token="${{basedir}}${{file.separator}}" value=""/>

            <!-- replace platform-dependent path separator by '/' -->
            <replace file="${{build.temp}}/classpath-temp.xml"
                    token="${{file.separator}}" value="/"/>

            <!-- now build the .classpath file -->
            <echo>Generate classpath</echo>
            <xslt   in="${{build.temp}}/classpath-temp.xml" out="${{basedir}}/.classpath"
                    processor="trax"
                    style="${{tools}}/ide/eclipse/make-classpath.xsl">
                <param name="exportlib" expression="${{ide.eclipse.export.libs}}"/>
            </xslt>

            <!-- copy the project file (expand version) -->
            <copy file="${{tools}}/ide/eclipse/project"
                    tofile="${{basedir}}/.project"
                    overwrite="yes">
                <filterset>
                    <filter token="VERSION" value="${{version}}"/>
                </filterset>
            </copy>
        </target>

        <xsl:apply-templates select="$cocoon-blocks"/>
    </xsl:template>

    <!-- For each project in gump.xml -->
    <xsl:template match="project">
        <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')"/>
        <xsl:variable name="cocoon-block-dependencies" select="depend[starts-with(@project,'cocoon-block-')]"/>

        <target name="{@name}-eclipseclasspath" unless="internal.exclude.block.{$block-name}">
            <eclipseclasspath-block name="{$block-name}"/>
            <!-- Add optional libraries used by this block -->
            <xsl:if test="library[not(@bundle='false')]">
                <xsl:for-each select="library[not(@bundle='false')]">
                    <include-lib-block name="{$block-name}" lib-name="{@name}"/>
                </xsl:for-each>
            </xsl:if>
        </target>
    </xsl:template>
</xsl:stylesheet>