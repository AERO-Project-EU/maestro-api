# Maestro Release
Guidelines needed in order to release a new version of the **Maestro**
platform.

## Prerequisits
0. Where `?.?.?` is appeared, it symbolizes the version.
1. Install java8
2. Install maven
3. Install python3.7 and later
4. Install npm
5. Install generate-changelog by running `npm install generate-changelog -g `


## Steps before publishing the new version
1. **Make sure** that the **project can be built**.
2. **Test all** the **functionalities** that are **working properly**, except the 
possible reported issues.
    - This is preferably to be **done on the development server**,
     due to the fact that a lot of features and tools (eg. Workspace,
      Consul, Kafka) can be tested only on a server.
    - Until an automated pipeline is possible to been built for the most
    of the features, it is **allowed** to **test small manageable changes** 
    between the versions **locally** and not on the server.
3. Check the **Dockerfiles** of all the **Maestro modules** that are able to 
**install** all the **prerequisites needed** for each of them (e.g. traefik for 
the Core-Orchestrator module).

## Modify project versions
1. **Configure** the **updateVersions.py** at the 
**ext-deps/releaseScripts** folder.
    - The `properties` dictionary is for configuring them in all the pom.xml 
    files that may exist.
    - The  `parentVersions` dictionary used in order to configure the parent 
    version of all the pom.xml files.
    - The `moduleVersion` dictionary used in order to configure the version 
    of each module, (eg. agent)
    - The `dependencyVersions` dictionary used in order to configure the 
    versions of the dependency used by the modules. **WARNING** the 
    dependencies will be changes in **ALL** the modules that use them.
2. Build once again the project and make sure all the modules was build 
correctly and no errors emerged.
3. If any external dependency versions changed, be sure no problems emerged 
and possibly retest the modules that use them. 
4. **Configure** the **updateDockerfiles.py** at the 
   **ext-deps/releaseScripts** folder.
   - The `jarVersions` dictionary used in order to replace the jar files
   inside the respective Dockerfiles

## Generate Changelog
1. Go at the project's parent directory
2. Run updateChangelog.py
    - If major version released `python3 updateChangelog.py -major`
    - If minor version released `python3 updateChangelog.py -minor`
    - if specific version tag released `python3 updateChangelog.py --version=?.?.?`
3. Check if anything is wrong with the **CHANGELOG.md** file.
    - Delete possible unneeded additions to the Changelog
    - In case that any problem or inconsistency on the versioning occur, 
    use the **-version** option.

## Finally, release the Kraken (Version)
1. Commit the changes 
    - Add all the files `git add .`
    - Commit `git commit -m "feat(framework): create version ?.?.?"`
    - Push changes `git push`
2. Create tag
    - `git tag -a ?.?.? -m "This is Maestro ?.?.? version of the platform"`
    -  `git push origin --tags`
3. From the UI create the release
    - **_~~TO BE FILLED~~_**