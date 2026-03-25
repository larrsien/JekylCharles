#define MyAppName "Amon Desktop Pet"
#define MyAppVersion "v1.0"
#define MyAppPublisher "Lars"
#define MyAppExeName "AmonDesktopPet.jar"

[Setup]
AppId={{A7D8B3E1-4F2C-4A9B-8E3D-1C5F6A7B8C9D}
AppName={#MyAppName}
AppVersion=1.0
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\AmonDesktopPet
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
SetupIconFile=amon.ico
OutputDir=..\installer-output
OutputBaseFilename=AmonDesktopPet-Setup
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
PrivilegesRequired=admin
DisableProgramGroupPage=yes
DisableWelcomePage=no
CloseApplications=yes

[UninstallRun]
Filename: "{sys}\taskkill.exe"; Parameters: "/F /IM javaw.exe"; Flags: runhidden; RunOnceId: "KillAmon"

[Languages]
Name: "russian"; MessagesFile: "compiler:Languages\Russian.isl"
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"
Name: "startup"; Description: "Запускать Амона при старте Windows"; GroupDescription: "Дополнительно:"

[Files]
Source: "..\target\AmonDesktopPet.jar"; DestDir: "{app}"; Flags: ignoreversion

Source: "jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs

Source: "..\native-messaging\com.lars.amon.browser.json"; DestDir: "{app}"; Flags: ignoreversion

Source: "..\browser-extension\manifest.json"; DestDir: "{app}\extension"; Flags: ignoreversion
Source: "..\browser-extension\background.js"; DestDir: "{app}\extension"; Flags: ignoreversion
Source: "..\browser-extension\icon128.png"; DestDir: "{app}\extension"; Flags: ignoreversion

; Инструкция по установке расширения
Source: "INSTALL_EXTENSION.html"; DestDir: "{app}"; Flags: ignoreversion

; Иконка для ярлыков
Source: "amon.ico"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
; Ярлык в меню Пуск
Name: "{group}\{#MyAppName}"; Filename: "{app}\jre\bin\javaw.exe"; Parameters: "--enable-native-access=ALL-UNNAMED -jar ""{app}\{#MyAppExeName}"""; WorkingDir: "{app}"; IconFilename: "{app}\amon.ico"
; Ярлык — инструкция по расширению
Name: "{group}\Установить расширение для браузера"; Filename: "{app}\INSTALL_EXTENSION.html"
; Ярлык — удаление
Name: "{group}\Удалить {#MyAppName}"; Filename: "{uninstallexe}"
; Ярлык на рабочем столе (если пользователь поставил галочку)
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\jre\bin\javaw.exe"; Parameters: "--enable-native-access=ALL-UNNAMED -jar ""{app}\{#MyAppExeName}"""; WorkingDir: "{app}"; IconFilename: "{app}\amon.ico"; Tasks: desktopicon

[Registry]
; Native Messaging — Chrome
Root: HKCU; Subkey: "Software\Google\Chrome\NativeMessagingHosts\com.lars.amon.browser"; ValueType: string; ValueName: ""; ValueData: "{app}\com.lars.amon.browser.json"; Flags: uninsdeletekey

; Native Messaging — Edge
Root: HKCU; Subkey: "Software\Microsoft\Edge\NativeMessagingHosts\com.lars.amon.browser"; ValueType: string; ValueName: ""; ValueData: "{app}\com.lars.amon.browser.json"; Flags: uninsdeletekey

; Native Messaging — Opera
Root: HKCU; Subkey: "Software\Opera Software\Opera Stable\NativeMessagingHosts\com.lars.amon.browser"; ValueType: string; ValueName: ""; ValueData: "{app}\com.lars.amon.browser.json"; Flags: uninsdeletekey

; Автозапуск при старте Windows (только если пользователь поставил галочку)
Root: HKCU; Subkey: "Software\Microsoft\Windows\CurrentVersion\Run"; ValueType: string; ValueName: "AmonDesktopPet"; ValueData: """{app}\jre\bin\javaw.exe"" --enable-native-access=ALL-UNNAMED -jar ""{app}\{#MyAppExeName}"""; Flags: uninsdeletevalue; Tasks: startup

[Run]
; Открыть инструкцию по расширению после установки
Filename: "{app}\INSTALL_EXTENSION.html"; Description: "Открыть инструкцию по установке расширения"; Flags: postinstall shellexec skipifsilent
; Запустить приложение сразу после установки
Filename: "{app}\jre\bin\javaw.exe"; Parameters: "--enable-native-access=ALL-UNNAMED -jar ""{app}\{#MyAppExeName}"""; WorkingDir: "{app}"; Description: "Запустить {#MyAppName}"; Flags: postinstall nowait skipifsilent

[Code]
function InitializeSetup(): Boolean;
begin
  Result := True;
end;

procedure CurStepChanged(CurStep: TSetupStep);
var
  ManifestFile: String;
  BatFile: String;
  AppPath: String;
  AppPathDoubleSlash: String;
  JsonContent: String;
begin
  if CurStep = ssPostInstall then
  begin
    AppPath := ExpandConstant('{app}');
    AppPathDoubleSlash := AppPath;
    StringChange(AppPathDoubleSlash, '\', '\\');

    BatFile := AppPath + '\amon-native-host.bat';
    SaveStringToFile(BatFile,
      '@echo off' + #13#10 +
      'setlocal' + #13#10 +
      'set "APP=' + AppPath + '"' + #13#10 +
      '"%APP%\jre\bin\java.exe" --enable-native-access=ALL-UNNAMED -cp "%APP%\AmonDesktopPet.jar" lars.com.browser.NativeMessagingBridge' + #13#10,
      False);

    ManifestFile := AppPath + '\com.lars.amon.browser.json';
    JsonContent :=
      '{' + #13#10 +
      '  "name": "com.lars.amon.browser",' + #13#10 +
      '  "description": "Amon Desktop Pet Browser Monitor",' + #13#10 +
      '  "path": "' + AppPathDoubleSlash + '\\amon-native-host.bat",' + #13#10 +
      '  "type": "stdio",' + #13#10 +
      '  "allowed_origins": ["chrome-extension://epdgpmgmlogldiaebaanjfnlfidgaehb/"]' + #13#10 +
      '}';
    SaveStringToFile(ManifestFile, AnsiString(JsonContent), False);
  end;
end;
