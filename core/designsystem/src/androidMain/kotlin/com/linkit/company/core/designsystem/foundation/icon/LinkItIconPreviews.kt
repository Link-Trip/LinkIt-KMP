package com.linkit.company.core.designsystem.foundation.icon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linkit.company.core.designsystem.theme.LinkItTheme

/** 아이콘 목록을 4개씩 격자로 렌더한다. 이름 라벨로 Figma와 대조하기 쉽게 한다. */
@Composable
private fun IconGrid(icons: List<Pair<String, ImageVector>>) {
    LinkItTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            icons.chunked(4).forEach { rowIcons ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowIcons.forEach { (name, icon) ->
                        Column(
                            modifier = Modifier.width(72.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = name,
                                modifier = Modifier.size(30.dp),
                                tint = LinkItTheme.color.atomic.Purple40,
                            )
                            Text(
                                text = name,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

// region Arrow

@Preview(showBackground = true)
@Composable
private fun LinkItIconArrowPreview() {
    IconGrid(
        listOf(
            "ChevronDown" to LinkItIcon.Arrow.ChevronDown,
            "ChevronDownSmall" to LinkItIcon.Arrow.ChevronDownSmall,
            "ChevronUp" to LinkItIcon.Arrow.ChevronUp,
            "ChevronUpSmall" to LinkItIcon.Arrow.ChevronUpSmall,
            "ChevronLeft" to LinkItIcon.Arrow.ChevronLeft,
            "ChevronLeftTight" to LinkItIcon.Arrow.ChevronLeftTight,
            "ChevronLeftSmall" to LinkItIcon.Arrow.ChevronLeftSmall,
            "ChevronLeftTightSmall" to LinkItIcon.Arrow.ChevronLeftTightSmall,
            "ChevronRight" to LinkItIcon.Arrow.ChevronRight,
            "ChevronRightTight" to LinkItIcon.Arrow.ChevronRightTight,
            "ChevronRightSmall" to LinkItIcon.Arrow.ChevronRightSmall,
            "ChevronRightTightSmall" to LinkItIcon.Arrow.ChevronRightTightSmall,
            "ArrowUp" to LinkItIcon.Arrow.ArrowUp,
            "ArrowDropDown" to LinkItIcon.Arrow.ArrowDropDown,
        ),
    )
}

// endregion

// region Communication

@Preview(showBackground = true)
@Composable
private fun LinkItIconCommunicationPreview() {
    IconGrid(
        listOf(
            "Message" to LinkItIcon.Communication.Message,
            "MessageFill" to LinkItIcon.Communication.MessageFill,
            "Like" to LinkItIcon.Communication.Like,
            "LikeFill" to LinkItIcon.Communication.LikeFill,
            "Dislike" to LinkItIcon.Communication.Dislike,
            "DislikeFill" to LinkItIcon.Communication.DislikeFill,
            "Person" to LinkItIcon.Communication.Person,
            "PersonFill" to LinkItIcon.Communication.PersonFill,
            "PersonPlus" to LinkItIcon.Communication.PersonPlus,
            "PersonPlusFill" to LinkItIcon.Communication.PersonPlusFill,
            "FaceSmile" to LinkItIcon.Communication.FaceSmile,
            "FaceSmileFill" to LinkItIcon.Communication.FaceSmileFill,
            "FaceAngry" to LinkItIcon.Communication.FaceAngry,
            "FaceAngryFill" to LinkItIcon.Communication.FaceAngryFill,
        ),
    )
}

// endregion

// region Location

@Preview(showBackground = true)
@Composable
private fun LinkItIconLocationPreview() {
    IconGrid(
        listOf(
            "Compass" to LinkItIcon.Location.Compass,
            "CompassFill" to LinkItIcon.Location.CompassFill,
            "Location" to LinkItIcon.Location.Location,
            "LocationFill" to LinkItIcon.Location.LocationFill,
            "LocationRefresh" to LinkItIcon.Location.LocationRefresh,
            "Map" to LinkItIcon.Location.Map,
            "MapSearch" to LinkItIcon.Location.MapSearch,
            "NavigationArrow" to LinkItIcon.Location.NavigationArrow,
            "LocationOff" to LinkItIcon.Location.LocationOff,
            "Train" to LinkItIcon.Location.Train,
        ),
    )
}

// endregion

// region Utility

@Preview(showBackground = true)
@Composable
private fun LinkItIconUtilityPreview() {
    IconGrid(
        listOf(
            "Bell" to LinkItIcon.Utility.Bell,
            "BellFill" to LinkItIcon.Utility.BellFill,
            "BellOff" to LinkItIcon.Utility.BellOff,
            "BellOffFill" to LinkItIcon.Utility.BellOffFill,
            "CirclePlus" to LinkItIcon.Utility.CirclePlus,
            "CirclePlusFill" to LinkItIcon.Utility.CirclePlusFill,
            "Clock" to LinkItIcon.Utility.Clock,
            "ClockFill" to LinkItIcon.Utility.ClockFill,
            "Close" to LinkItIcon.Utility.Close,
            "CloseThick" to LinkItIcon.Utility.CloseThick,
            "CircleClose" to LinkItIcon.Utility.CircleClose,
            "CircleCloseFill" to LinkItIcon.Utility.CircleCloseFill,
            "CircleCheck" to LinkItIcon.Utility.CircleCheck,
            "CircleCheckFill" to LinkItIcon.Utility.CircleCheckFill,
            "CircleQuestion" to LinkItIcon.Utility.CircleQuestion,
            "CircleQuestionFill" to LinkItIcon.Utility.CircleQuestionFill,
            "CircleInfo" to LinkItIcon.Utility.CircleInfo,
            "CircleInfoFill" to LinkItIcon.Utility.CircleInfoFill,
            "CircleExclamation" to LinkItIcon.Utility.CircleExclamation,
            "CircleExclamationFill" to LinkItIcon.Utility.CircleExclamationFill,
            "Circle" to LinkItIcon.Utility.Circle,
            "CircleFill" to LinkItIcon.Utility.CircleFill,
            "Check" to LinkItIcon.Utility.Check,
            "CheckThick" to LinkItIcon.Utility.CheckThick,
            "Folder" to LinkItIcon.Utility.Folder,
            "FolderFill" to LinkItIcon.Utility.FolderFill,
            "FolderStar" to LinkItIcon.Utility.FolderStar,
            "FolderStarFill" to LinkItIcon.Utility.FolderStarFill,
            "MoreHorizontal" to LinkItIcon.Utility.MoreHorizontal,
            "MoreVertical" to LinkItIcon.Utility.MoreVertical,
            "MoreVerticalTight" to LinkItIcon.Utility.MoreVerticalTight,
            "Search" to LinkItIcon.Utility.Search,
            "Money" to LinkItIcon.Utility.Money,
            "Ai" to LinkItIcon.Utility.Ai,
            "Archive" to LinkItIcon.Utility.Archive,
            "FolderJob" to LinkItIcon.Utility.FolderJob,
            "Calendar" to LinkItIcon.Utility.Calendar,
            "ImageAdd" to LinkItIcon.Utility.ImageAdd,
            "Image" to LinkItIcon.Utility.Image,
            "RadioChecked" to LinkItIcon.Utility.RadioChecked,
            "Ban" to LinkItIcon.Utility.Ban,
            "Radio" to LinkItIcon.Utility.Radio,
        ),
    )
}

// endregion

// region Control

@Preview(showBackground = true)
@Composable
private fun LinkItIconControlPreview() {
    IconGrid(
        listOf(
            "Star" to LinkItIcon.Control.Star,
            "StarFill" to LinkItIcon.Control.StarFill,
            "Bookmark" to LinkItIcon.Control.Bookmark,
            "BookmarkFill" to LinkItIcon.Control.BookmarkFill,
            "Home" to LinkItIcon.Control.Home,
            "HomeFill" to LinkItIcon.Control.HomeFill,
            "EyeSlash" to LinkItIcon.Control.EyeSlash,
            "EyeSlashFill" to LinkItIcon.Control.EyeSlashFill,
            "Eye" to LinkItIcon.Control.Eye,
            "EyeFill" to LinkItIcon.Control.EyeFill,
            "Heart" to LinkItIcon.Control.Heart,
            "HeartFill" to LinkItIcon.Control.HeartFill,
            "Filter" to LinkItIcon.Control.Filter,
            "FilterFill" to LinkItIcon.Control.FilterFill,
            "Pencil" to LinkItIcon.Control.Pencil,
            "PencilFill" to LinkItIcon.Control.PencilFill,
            "FilterLine" to LinkItIcon.Control.FilterLine,
            "Refresh" to LinkItIcon.Control.Refresh,
            "Upload" to LinkItIcon.Control.Upload,
            "Attachment" to LinkItIcon.Control.Attachment,
            "Write" to LinkItIcon.Control.Write,
            "Reset" to LinkItIcon.Control.Reset,
            "BellPlus" to LinkItIcon.Control.BellPlus,
            "Setting" to LinkItIcon.Control.Setting,
            "Share" to LinkItIcon.Control.Share,
            "ShareIos" to LinkItIcon.Control.ShareIos,
            "Image" to LinkItIcon.Control.Image,
            "Calendar" to LinkItIcon.Control.Calendar,
            "Inbox" to LinkItIcon.Control.Inbox,
            "Link" to LinkItIcon.Control.Link,
            "Change" to LinkItIcon.Control.Change,
            "Download" to LinkItIcon.Control.Download,
            "ExternalLink" to LinkItIcon.Control.ExternalLink,
            "Trash" to LinkItIcon.Control.Trash,
            "Tune" to LinkItIcon.Control.Tune,
            "FlipBackward" to LinkItIcon.Control.FlipBackward,
            "Full" to LinkItIcon.Control.Full,
            "Leave" to LinkItIcon.Control.Leave,
            "Search" to LinkItIcon.Control.Search,
            "SearchThick" to LinkItIcon.Control.SearchThick,
            "Copy" to LinkItIcon.Control.Copy,
            "Customize" to LinkItIcon.Control.Customize,
        ),
    )
}

// endregion
