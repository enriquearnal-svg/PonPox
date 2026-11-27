#ifndef PackageDir
  #error PackageDir no definido
#endif
#ifndef PrivateData
  #error PrivateData no definido
#endif
#ifndef OutputPath
  #error OutputPath no definido
#endif
#define ProductVersion "31.0.0"

[Setup]
AppId=PonPox.Windows
AppName=PonPox
AppVersion={#ProductVersion}
AppPublisher=PonPox
DefaultDirName={localappdata}\Programs\PonPox
DefaultGroupName=PonPox
DisableDirPage=yes
DisableProgramGroupPage=yes
PrivilegesRequired=lowest
ArchitecturesAllowed=x64os
ArchitecturesInstallIn64BitMode=x64os
MinVersion=10.0.17763
OutputDir={#OutputPath}
OutputBaseFilename=PonPox_Setup
SetupIconFile={#PackageDir}\PonPox.ico
UninstallDisplayIcon={app}\PonPox.exe
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
CloseApplications=yes
RestartApplications=no
ShowLanguageDialog=no

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Files]
Source: "{#PackageDir}\PonPox\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#PackageDir}\Manual.pdf"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#PrivateData}\Informes\*"; DestDir: "{%USERPROFILE}\PonPox\Informes"; Flags: recursesubdirs createallsubdirs onlyifdoesntexist uninsneveruninstall skipifsourcedoesntexist; Check: InstalarDatos
Source: "{#PrivateData}\fotos trabajadores\*"; DestDir: "{%USERPROFILE}\.ponpox\fotos trabajadores"; Flags: recursesubdirs onlyifdoesntexist uninsneveruninstall skipifsourcedoesntexist; Check: InstalarDatos
Source: "{#PrivateData}\ponpox.db"; DestDir: "{%USERPROFILE}\.ponpox"; Flags: onlyifdoesntexist uninsneveruninstall; Check: InstalarDatos
Source: "{#PrivateData}\instalacion-datos.txt"; DestDir: "{%USERPROFILE}\.ponpox"; Flags: onlyifdoesntexist uninsneveruninstall; Check: InstalarDatos

[Icons]
Name: "{autoprograms}\PonPox\PonPox"; Filename: "{app}\PonPox.exe"; WorkingDir: "{app}\app"
Name: "{autoprograms}\PonPox\Manual de PonPox"; Filename: "{app}\Manual.pdf"
Name: "{autodesktop}\PonPox"; Filename: "{app}\PonPox.exe"; WorkingDir: "{app}\app"

[Run]
Filename: "{app}\PonPox.exe"; WorkingDir: "{app}\app"; Description: "Abrir PonPox"; Flags: nowait postinstall skipifsilent

[Code]
var
  Sembrar: Boolean;

function PrepareToInstall(var NeedsRestart: Boolean): String;
var
  Datos: String;
begin
  Datos := ExpandConstant('{%USERPROFILE}\.ponpox');
  Sembrar := not FileExists(Datos + '\ponpox.db')
    and not FileExists(Datos + '\instalacion-datos.txt');
  Result := '';
  if not Sembrar and not FileExists(Datos + '\ponpox.db') then
    Result := 'Falta la base de datos local de PonPox. Restaura tu copia de seguridad antes de actualizar.';
end;

function InstalarDatos: Boolean;
begin
  Result := Sembrar;
end;
